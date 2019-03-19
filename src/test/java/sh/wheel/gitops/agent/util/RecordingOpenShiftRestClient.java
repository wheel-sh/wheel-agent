package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import okhttp3.OkHttpClient;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RecordingOpenShiftRestClient extends OpenShiftRestClient {

    private final Path targetDir;
    private final static ObjectMapper OM;

    static {
        OM = new ObjectMapper();
        OM.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public RecordingOpenShiftRestClient(Path targetDir, String apiServerUrl, RestTemplate restTemplate) {
        super(apiServerUrl, restTemplate);
        this.targetDir = targetDir;
    }

    public static RecordingOpenShiftRestClient createRecordingClient(Path recordTargetDir)  {
        try {
            Config config = Config.autoConfigure(null);
            Config sslConfig = new ConfigBuilder(config)
                    .withMasterUrl(config.getMasterUrl())
                    .withRequestTimeout(5000)
                    .withConnectionTimeout(5000)
                    .build();
            OkHttpClient client = HttpClientUtils.createHttpClient(sslConfig);
            OkHttp3ClientHttpRequestFactory requestFactory = new OkHttp3ClientHttpRequestFactory(client);
            Files.createDirectories(recordTargetDir);
            return new RecordingOpenShiftRestClient(recordTargetDir, config.getMasterUrl(), new RestTemplate(requestFactory));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    JsonNode patch(String endpoint, Object patchObject) {
        JsonNode response = super.patch(endpoint, patchObject);
        record("patch", endpoint, patchObject, response);
        return response;
    }

    @Override
    JsonNode delete(String url) {
        JsonNode delete = super.delete(url);
        record("delete", url, null, delete);
        return delete;
    }

    @Override
    JsonNode post(String endpoint, Object postRequest) {
        JsonNode post = super.post(endpoint, postRequest);
        record("post", endpoint, postRequest, post);
        return post;
    }

    @Override
    JsonNode get(String url) {
        JsonNode get = super.get(url);
        record("get", url, null, get);
        return get;
    }

    private void record(String type, String endpoint, Object requestObject, JsonNode response) {
        try {
            String endpointWithoutArgs = endpoint;
            int endIndex = endpointWithoutArgs.lastIndexOf("?");
            if(endIndex > 0) {
                endpointWithoutArgs = endpoint.substring(0, endIndex);
            }
            String endpointReplaced = endpointWithoutArgs.replaceAll("/", "_")
                    .replaceAll("\\\\", "_");
            JsonNode request = requestObject != null ? OM.readTree(requestObject.toString()) : null;
            Path file = targetDir.resolve("call_" + type + endpointReplaced + ".json");
            RecordedRequestResponse recordedRequestResponse = new RecordedRequestResponse(endpoint, type, request, null, response);
            String json = OM.writeValueAsString(recordedRequestResponse);
            Files.write(file, json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
