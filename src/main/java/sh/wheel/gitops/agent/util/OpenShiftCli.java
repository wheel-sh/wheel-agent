package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class OpenShiftCli {
    public static final String API_RESOURCES_WIDE = "oc api-resources -owide --no-headers";
    private static final String BASIC_GET_ARGS = " -ojson --ignore-not-found";
    private static final String EXPORT_ARG = " --export";
    private static final String GET_RESOURCES = "oc get ${kind} -n ${project}" + BASIC_GET_ARGS;
    private static final String GET_RESOURCE = "oc get ${kind} ${name} -n ${project}" + BASIC_GET_ARGS;
    private static final String GET_API_RESOURCE = "oc api-resources -o name";
    private static final String NAMESPACED_ARG = " --namespaced=${namespaced}";
    private static final String PROCESS_TEMPLATE = "oc process -f ${path} --local";
    private static final String WHOAMI = "oc whoami";
    private static final String CAN_I = "oc auth can-i ${verb} ${resource}";


    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String METHOD_NOT_ALLOWED_ERROR_STR = "Error from server (MethodNotAllowed): the server does not allow this method on the requested resource";
    public static final String FORBIDDEN_ERROR_STR = "Error from server (Forbidden)";
    public static final String BAD_REQUEST_ERROR_STR = "Error from server (BadRequest)";


    public JsonNode getResourceList(String kind, String project) {
        Map<String, String> substitutionMap = new HashMap<>();
        substitutionMap.put("kind", kind);
        substitutionMap.put("project", project);
        return execToJsonNode(addExportArgIfSupported(GET_RESOURCES, kind), substitutionMap);
    }

    public JsonNode getResource(String kind, String name, String project) {
        Map<String, String> substitutionMap = new HashMap<>();
        substitutionMap.put("kind", kind);
        substitutionMap.put("project", project);
        substitutionMap.put("name", name);
        return execToJsonNode(addExportArgIfSupported(GET_RESOURCE, kind), substitutionMap);
    }

    public List<JsonNode> getManageableProjects() {
        JsonNode jsonNode = execToJsonNode("oc get projects -ojson --export");
        return StreamSupport.stream(jsonNode.get("items").spliterator(), false).collect(Collectors.toList());
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

    public List<String> getApiResourcesWide(boolean namespaced) {
        Map<String, String> substitutionMap = new HashMap<>();
        substitutionMap.put("namespaced", String.valueOf(namespaced));
        String s = execToString(API_RESOURCES_WIDE + NAMESPACED_ARG, substitutionMap);
        String[] apiResources = s.split(System.getProperty("line.separator"));
        return Arrays.asList(apiResources);
    }

    public JsonNode process(String templatePath, Map<String, String> params) {
        StringBuilder command = new StringBuilder(PROCESS_TEMPLATE + " ");
        for (Map.Entry<String, String> e : params.entrySet()) {
            command.append("-p ").append(e.getKey()).append("=").append("\'").append(e.getValue()).append("\'").append(" ");
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("path", templatePath);
        return execToJsonNode(command.toString(), map);
    }


    public List<JsonNode> getResources(String project, List<String> apiResources) {
        final AtomicLong max = new AtomicLong();
        long start = System.nanoTime();
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
            } catch (MethodErrorException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String addExportArgIfSupported(String command, String kind) {
        List<String> unsupportedExport = Arrays.asList("secrets", "projects");
        if (unsupportedExport.contains(kind)) {
            command += EXPORT_ARG;
        }
        return command;
    }

    public String getWhoAmI() {
        return execToString(WHOAMI, null).trim();
    }

    public boolean canI(String verb, String resource)  {
        try {
            Map<String, String> substitutionMap = new HashMap<>();
            substitutionMap.put("verb", verb);
            substitutionMap.put("resource", resource);
            CommandLine commandline = CommandLine.parse(CAN_I, substitutionMap);
            DefaultExecutor exec = new DefaultExecutor();
            exec.setExitValues(new int[]{0, 1});
            int execute = exec.execute(commandline);
            return execute == 0;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
            String errorMessage = outputStream.toString().trim();
            StringBuffer executedCommand = StringUtils.stringSubstitution(command, substitutionMap, true);
            if (METHOD_NOT_ALLOWED_ERROR_STR.equals(errorMessage)) {
                throw new MethodErrorException("Command not allowed (MethodNotAllowed) '" + executedCommand + "'");
            }
            if (errorMessage.startsWith(FORBIDDEN_ERROR_STR)) {
                throw new MethodErrorException("Command forbidden (Forbidden) '" + executedCommand + "'");
            }
            if (errorMessage.startsWith(BAD_REQUEST_ERROR_STR)) {
                throw new MethodErrorException("Command forbidden (Forbidden) '" + executedCommand + "'");
            }
            LOG.error("Error while executing command '" + executedCommand);
            LOG.error("Console output:\n" + outputStream.toString());
            throw new OpenShiftCliException(e);
        }
    }

}
