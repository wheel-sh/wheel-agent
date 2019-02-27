package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OpenShiftCli {
    private static final String BASIC_GET_ARGUMENTS = "-ojson --ignore-not-found --export";
    private static final String GET_RESOURCES = "oc get ${kind} -n ${namespace} " + BASIC_GET_ARGUMENTS;

    public static JsonNode get(String resourceName, String namespace) throws IOException {
        String s = execToString("oc get " + resourceName + " -n " + namespace + " -ojson --ignore-not-found --export");
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(s);
    }


    public static String execToString(String command) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CommandLine commandline = CommandLine.parse(command);
        DefaultExecutor exec = new DefaultExecutor();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        exec.setStreamHandler(streamHandler);
        int execute = exec.execute(commandline);
        return (outputStream.toString());
    }

}
