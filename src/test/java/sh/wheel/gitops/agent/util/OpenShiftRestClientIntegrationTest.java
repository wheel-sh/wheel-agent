package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import sh.wheel.gitops.agent.model.ApiResource;
import sh.wheel.gitops.agent.model.Resource;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenShiftRestClientIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private OpenShiftRestClient openShiftRestClient;

    @BeforeEach
    void setUp() {
        Config config = Config.autoConfigure(null);
        Config sslConfig = new ConfigBuilder(config)
                .withMasterUrl(config.getMasterUrl())
                .withRequestTimeout(5000)
                .withConnectionTimeout(5000)
                .build();
        OkHttpClient client = HttpClientUtils.createHttpClient(sslConfig);
        OkHttp3ClientHttpRequestFactory requestFactory = new OkHttp3ClientHttpRequestFactory(client);
        RestTemplate template = new RestTemplate(requestFactory) {
            @Override
            public <T> ResponseEntity<T> exchange(String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) throws RestClientException {
                LOG.info("exchange request " + requestEntity);
                ResponseEntity<T> exchange = super.exchange(url, method, requestEntity, responseType, uriVariables);
                LOG.info("exchange response "+ exchange);
                return exchange;
            }

        };

        openShiftRestClient = new OpenShiftRestClient(config.getMasterUrl(), template);
    }

    @Test
    void getOkclient() throws IOException {
        Config config = Config.autoConfigure(null);
        Config sslConfig = new ConfigBuilder(config)
                .withMasterUrl(config.getMasterUrl())
                .withRequestTimeout(5000)
                .withConnectionTimeout(5000)
                .build();

        OkHttpClient client = HttpClientUtils.createHttpClient(config);
        Request request = new Request.Builder().get().url(sslConfig.getMasterUrl())
                .build();
        Response response = client.newCall(request).execute();
        try (ResponseBody body = response.body()) {
            System.out.println(response.isSuccessful());
        }
    }

    @Test
    void getAllProjects() {
        List<JsonNode> allProjects = openShiftRestClient.getAllProjects();
        assertNotNull(allProjects);
    }

    @Test
    void getAllApiResources() {
        List<ApiResource> allApiResources = openShiftRestClient.getAllApiResources();
        assertNotNull(allApiResources);
    }

    @Test
    void getFilteredApiResources() {
        List<String> requiredOperations = Arrays.asList("create", "delete", "get", "list", "patch", "update", "watch");
        List<ApiResource> allApiResources = openShiftRestClient.getFilteredApiResources(true, requiredOperations);
        assertNotNull(allApiResources);
    }

    @Test
    void getManageableResources() {
        List<String> requiredVerbs = Arrays.asList("create", "delete", "get", "list", "patch", "update", "watch");
        long start = System.currentTimeMillis();
        List<ApiResource> apiResources = openShiftRestClient.getFilteredApiResources(true, requiredVerbs);
        List<ApiResource> manageableResources = openShiftRestClient.fetchManageableResources(openShiftRestClient.whoAmI(), "example-app-test", requiredVerbs, apiResources);
        System.out.println("Millis: " + (System.currentTimeMillis() - start));
        assertNotNull(manageableResources);
    }

    @Test
    void whoAmI() {
        String s = openShiftRestClient.whoAmI();
        assertNotNull(s);
    }

    @Test
    @Disabled
    void fetchResources() {
        ApiResource dcv1beta1 = ApiResource.newBuilder()
                .name("deployments")
                .kind("Deployment")
                .groupName("extensions")
                .apiVersion("extensions/v1beta1")
                .coreApi(false)
                .subresource(false)
                .namespaced(true)
                .build();
        ApiResource dcv1beta2 = ApiResource.newBuilder()
                .name("deployments")
                .kind("Deployment")
                .groupName("apps")
                .apiVersion("apps/v1beta2")
                .coreApi(false)
                .subresource(false)
                .namespaced(true)
                .build();
        ApiResource dcv1 = ApiResource.newBuilder()
                .name("deployments")
                .kind("Deployment")
                .groupName("apps")
                .apiVersion("apps/v1")
                .coreApi(false)
                .subresource(false)
                .namespaced(true)
                .build();

        List<Resource> resourcesv1beta1 = openShiftRestClient.fetchNamespacedResourceList(dcv1beta1, "chartmuseum");
        List<Resource> resourcesv1beta2 = openShiftRestClient.fetchNamespacedResourceList(dcv1beta2, "chartmuseum");
        List<Resource> resourcesv1 = openShiftRestClient.fetchNamespacedResourceList(dcv1, "chartmuseum");

        assertNotNull(resourcesv1beta1);
    }

    @Test
    void fetchAllManageableResourcesInNamespace() {
        List<String> requiredVerbs = Arrays.asList("create", "delete", "get", "list", "patch", "update", "watch");
        long start = System.currentTimeMillis();
        List<ApiResource> apiResources = openShiftRestClient.getFilteredApiResources(true, requiredVerbs);
        List<ApiResource> manageableResources = openShiftRestClient.fetchManageableResources(openShiftRestClient.whoAmI(), "example-app-test", requiredVerbs, apiResources);
        List<Resource> resources = openShiftRestClient.fetchResourcesFromNamespace(manageableResources, "example-app-test");

        assertNotNull(resources);
    }

    @Test
    void fetchResource() {
        String projectName = "example-app-test";
        Resource r = openShiftRestClient.fetchProject(projectName);

        assertNotNull(r);
    }

    @Test
    void createDelete() throws IOException {
        ObjectMapper om = new ObjectMapper();
        JsonNode secret = om.readTree("{ \"apiVersion\": \"v1\", \"kind\": \"Secret\", \"metadata\": { \"name\": \"test-secret\" }, \"type\": \"Opaque\" }\n");

        openShiftRestClient.post("/api/v1/namespaces/example-app-test/secrets", secret);
        JsonNode secretReference = om.readTree("{ \"apiVersion\": \"v1\", \"kind\": \"Secret\", \"metadata\": { \"creationTimestamp\": \"2019-03-13T08:19:31Z\", \"name\": \"test-secret\", \"namespace\": \"example-app-test\", \"resourceVersion\": \"9861108\", \"selfLink\": \"/api/v1/namespaces/example-app-test/secrets/test-secret\", \"uid\": \"ba1ec5b4-4568-11e9-8ded-0200c0a87ac9\" }, \"type\": \"Opaque\"}");
        JsonNode delete = openShiftRestClient.delete(new Resource(null, null, secretReference));

        assertNotNull(delete);
    }

    @Test
    void createFetchDeleteProject() {
        String projectName = "junit-test-project";
        JsonNode newProject = openShiftRestClient.newProject(projectName);
        Resource fetchProject = openShiftRestClient.fetchProject(projectName);
        openShiftRestClient.delete(fetchProject);
        Resource fetchProjectAfterDeletion = openShiftRestClient.fetchProject(projectName);
        String phase = fetchProjectAfterDeletion.getJsonNode().get("status").get("phase").textValue();
        assertEquals("Terminating", phase);
    }
}