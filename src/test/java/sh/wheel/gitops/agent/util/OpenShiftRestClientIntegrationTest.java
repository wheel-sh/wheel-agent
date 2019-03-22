package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.model.ApiResource;
import sh.wheel.gitops.agent.model.Resource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenShiftRestClientIntegrationTest {

    private OpenShiftRestClient openShiftRestClient;

    @BeforeEach
    void setUp() throws URISyntaxException {
        Path mockDataDir = Paths.get(this.getClass().getResource("/").toURI()).resolve("samples").resolve("mock_data4");
        openShiftRestClient = RecordingOpenShiftRestClient.createRecordingClient(mockDataDir);
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
        ApiResource dcv1beta1 = ApiResource.builder()
                .name("deployments")
                .kind("Deployment")
                .groupName("extensions")
                .apiVersion("extensions/v1beta1")
                .coreApi(false)
                .subresource(false)
                .namespaced(true)
                .build();
        ApiResource dcv1beta2 = ApiResource.builder()
                .name("deployments")
                .kind("Deployment")
                .groupName("apps")
                .apiVersion("apps/v1beta2")
                .coreApi(false)
                .subresource(false)
                .namespaced(true)
                .build();
        ApiResource dcv1 = ApiResource.builder()
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
        assertNotNull(resourcesv1beta2);
        assertNotNull(resourcesv1);
    }

    @Test
    void fetchAllManageableResourcesInNamespace() {
        List<String> requiredVerbs = Arrays.asList("create", "delete", "get", "list", "patch", "update", "watch");
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
        Resource fetchProject = openShiftRestClient.fetchProject(projectName);
        openShiftRestClient.delete(fetchProject);
        Resource fetchProjectAfterDeletion = openShiftRestClient.fetchProject(projectName);
        String phase = fetchProjectAfterDeletion.getJsonNode().get("status").get("phase").textValue();
        assertEquals("Terminating", phase);
    }
}