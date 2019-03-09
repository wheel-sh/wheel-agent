package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class OpenShiftCli {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String API_RESOURCES_WIDE = "oc api-resources -owide --no-headers";
    private static final String METHOD_NOT_ALLOWED_ERROR_STR = "Error from server (MethodNotAllowed): the server does not allow this method on the requested resource";
    private static final String FORBIDDEN_ERROR_STR = "Error from server (Forbidden)";
    private static final String BAD_REQUEST_ERROR_STR = "Error from server (BadRequest)";
    private static final String BASIC_GET_ARGS = " -ojson --ignore-not-found";
    private static final String EXPORT_ARG = " --export";
    private static final String GET_RESOURCES = "oc get ${kind} -n ${project}" + BASIC_GET_ARGS;
    private static final String GET_RESOURCE = "oc get ${kind} ${name} -n ${project}" + BASIC_GET_ARGS;
    private static final String GET_API_RESOURCE = "oc api-resources -o name";
    private static final String NAMESPACED_ARG = " --namespaced=${namespaced}";
    private static final String PROCESS_TEMPLATE = "oc process -f ${path} --local";
    private static final String WHOAMI = "oc whoami";
    private static final String CAN_I = "oc auth can-i ${verb} ${resource}";
    private static final String OC_APPLY = "oc apply -f - -n ${project}";
    private static final String OC_DELETE = "oc delete ${kind} ${name} -n ${project}";
    private static final String OC_NEW_PROJECT = "oc new-project ${project}";
    private static final String OC_LOGIN = "oc login https://kubernetes.default.svc:443 --token=${token}";

    public OpenShiftCli() {
        // hackerio :-/
        login();
    }

    public void login() {
        try {
            String whoAmI = getWhoAmI();
            if(!whoAmI.isEmpty()) {
                return;
            }
        } catch (Exception e) {
            LOG.debug("Exception occured while calling getWhoAmiI()", e);
        }
        Path userHome = Paths.get(System.getProperty("user.home"));
        Path kubeConfig = userHome.resolve(".kube/config");
        if(!Files.exists(kubeConfig)) {
            Path tokenPath = Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/token");
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                String token = Files.lines(tokenPath).findFirst().orElse(null);
                Map<String, String> substitutionMap = new HashMap<>();
                substitutionMap.put("token", token);
                DefaultExecutor exec = new DefaultExecutor();
                PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
                exec.setStreamHandler(streamHandler);
                CommandLine commandline = CommandLine.parse(OC_LOGIN, substitutionMap);
                int execute = exec.execute(commandline);
                LOG.info("Output of oc login: " + outputStream.toString());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

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
        String whoAmiI = getWhoAmI();
        return StreamSupport.stream(jsonNode.get("items").spliterator(), false)
                .filter(jn -> isCallerRequestor(jn, whoAmiI))
                .collect(Collectors.toList());
    }

    private boolean isCallerRequestor(JsonNode project, String whoAmiI) {
        JsonNode jsonNode = project.get("metadata").get("annotations");
        if(jsonNode != null) {
            JsonNode requestor = jsonNode.get("openshift.io/requester");
            if(requestor != null) {
                String requestorName = requestor.textValue();
                return whoAmiI.equals(requestorName);
            }
        }
        return false;
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


    public List<JsonNode> getResources(String project, Set<String> apiResources) {
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

    public boolean canI(String verb, String resource) {
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

    public void apply(String projectName, JsonNode jsonNode) {
        try {
            Map<String, String> substitutionMap = new HashMap<>();
            substitutionMap.put("project", projectName);
            CommandLine command = CommandLine.parse(OC_APPLY, substitutionMap);
            Executor exec = new DefaultExecutor();
            ByteArrayInputStream input = new ByteArrayInputStream(jsonNode.toString().getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            exec.setStreamHandler(new PumpStreamHandler(output, null, input));
            exec.execute(command);
            LOG.info(String.format("Output of '%s':\n%s", command.toString(), output));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void newProject(String projectName) {
        Map<String, String> substitutionMap = new HashMap<>();
        substitutionMap.put("project", projectName);
        String s = execToString(OC_NEW_PROJECT, substitutionMap);
        StringBuffer executedCommand = StringUtils.stringSubstitution(OC_NEW_PROJECT, substitutionMap, true);
        LOG.info(String.format("Output of command '%s':\n", executedCommand, s));
    }

    public void delete(String projectName, String kind, String name) {
        Map<String, String> substitutionMap = new HashMap<>();
        substitutionMap.put("project", projectName);
        substitutionMap.put("kind", kind);
        substitutionMap.put("name", name);
        StringBuffer executedCommand = StringUtils.stringSubstitution(OC_DELETE, substitutionMap, true);
        String s = execToString(OC_DELETE, substitutionMap);
        LOG.info(String.format("Output of command '%s':\n", executedCommand, s));
    }
}
