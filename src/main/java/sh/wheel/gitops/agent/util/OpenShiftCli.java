package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class OpenShiftCli {
    private static final String BASIC_GET_ARGS = " -ojson --ignore-not-found";
    private static final String EXPORT_ARG = " --export";
    private static final String GET_RESOURCES = "oc get ${kind} -n ${project}" + BASIC_GET_ARGS;
    private static final String GET_RESOURCE = "oc get ${kind} ${name} -n ${project}" + BASIC_GET_ARGS;
    private static final String GET_API_RESOURCE = "oc api-resources -o name";
    private static final String NAMESPACED_ARG = " --namespaced=${namespaced}";
    private static final String PROCESS_TEMPLATE = "oc process -f ${path} --local";


    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String METHOD_NOT_ALLOWED_ERROR_STR = "Error from server (MethodNotAllowed): the server does not allow this method on the requested resource";


    public JsonNode getResourceList(String kind, String project) {
        Map<String, String> substitutionMap = new HashMap<>();
        substitutionMap.put("kind", kind);
        substitutionMap.put("project", project);
        return execToJsonNode(addExportArgIfNotSecret(GET_RESOURCES, kind), substitutionMap);
    }

    public JsonNode getResource(String kind, String name, String project) {
        Map<String, String> substitutionMap = new HashMap<>();
        substitutionMap.put("kind", kind);
        substitutionMap.put("project", project);
        substitutionMap.put("name", name);
        return execToJsonNode(addExportArgIfNotSecret(GET_RESOURCE, kind), substitutionMap);
    }

    public List<String> getAllApiResources() {
        String s = execToString(GET_API_RESOURCE, null);
        String[] apiResources = s.split(System.getProperty("line.separator"));
        return Arrays.asList(apiResources);
    }

    public List<String> getApiResources(boolean namespaced) {
        Map<String, String> substitutionMap = new HashMap<>();
        substitutionMap.put("namespaced", String.valueOf(namespaced));
        String s = execToString(GET_API_RESOURCE + NAMESPACED_ARG, substitutionMap);
        String[] apiResources = s.split(System.getProperty("line.separator"));
        return Arrays.asList(apiResources);
    }

    public JsonNode process(String templatePath, Map<String, String> params) {
        StringBuilder command = new StringBuilder(PROCESS_TEMPLATE +" ");
        for (Map.Entry<String, String> e : params.entrySet()) {
            command.append("-p ").append(e.getKey()).append("=").append("\'").append(e.getValue()).append("\'").append(" ");
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("path", templatePath);
        return execToJsonNode(command.toString(), map);
    }

    public List<JsonNode> getAllNamespacedResource(String project) {
        final AtomicLong max = new AtomicLong();
        long start = System.nanoTime();
        List<String> apiResources = getApiResources(true).stream().filter(ar -> !ar.endsWith("security.openshift.io")).collect(Collectors.toList());
        return apiResources.stream().parallel().map(ar -> {
            try {
                long reqStart = System.nanoTime();
                JsonNode resourceList = getResourceList(ar, project);
                long reqTime = System.nanoTime() - reqStart;
                if (reqTime > max.longValue()) {
                    max.set(reqTime);
                    System.out.println("New Max time: " + reqTime + " - " + ar);
                }
                return resourceList;
            } catch (MethodNotAllowedException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

        private String addExportArgIfNotSecret(String command, String kind) {
        if (!"secret".equals(kind.trim()) && !"secrets".equals(kind.trim())) {
            command += EXPORT_ARG;
        }
        return command;
    }

    private JsonNode execToJsonNode(String command, Map<String, ?> substitutionMap) {
        String s = execToString(command, substitutionMap);
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(s);
        } catch (IOException e) {
            LOG.error("Error while executing command '" + StringUtils.stringSubstitution(command, substitutionMap, true));
            LOG.error("JSON input: \n" + s);
            throw new OpenShiftCliException(e);
        }
    }

    private JsonNode execToJsonNode(String command) {
        return execToJsonNode(command, null);
    }

    private String execToString(String command, Map<String, ?> substitutionMap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            CommandLine commandline = CommandLine.parse(command, substitutionMap);
            DefaultExecutor exec = new DefaultExecutor();
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            exec.setStreamHandler(streamHandler);
            int execute = exec.execute(commandline);
            return (outputStream.toString());
        } catch (Exception e) {
            if(METHOD_NOT_ALLOWED_ERROR_STR.equals(outputStream.toString().trim())) {
                throw new MethodNotAllowedException("Command not allowed (MethodNotAllowed) '" + StringUtils.stringSubstitution(command, substitutionMap, true));
            }
            LOG.error("Error while executing command '" + StringUtils.stringSubstitution(command, substitutionMap, true));
            LOG.error("Console output:\n" + outputStream.toString());
            throw new OpenShiftCliException(e);
        }
    }

}
