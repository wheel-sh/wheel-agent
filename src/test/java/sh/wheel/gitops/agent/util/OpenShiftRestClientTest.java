package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

class OpenShiftRestClientTest {

    private OpenShiftRestClient openShiftRestClient;

    @BeforeEach
    void setUp() throws URISyntaxException {
        Path mockDataDir = Paths.get(this.getClass().getResource("/").toURI()).resolve("samples").resolve("mock-data-1");
        openShiftRestClient = MockOpenShiftRestClient.createMockClient(mockDataDir);
    }

    @Test
    void getAllProjects() {
        List<JsonNode> allProjects = openShiftRestClient.getAllProjects();

        assertEquals(1, allProjects.size());
        JsonNode project = allProjects.get(0);
        assertEquals("example-app-test", project.get("metadata").get("name").textValue());
    }

    @Test
    void getAllApiResources() {
        List<ApiResource> allApiResources = openShiftRestClient.getAllApiResources();
        assertEquals(244, allApiResources.size());
    }

    @Test
    void getFilteredApiResources() {
        List<String> requiredOperations = Arrays.asList("create", "delete", "get", "list", "patch", "update", "watch");
        List<ApiResource> allApiResources = openShiftRestClient.getFilteredApiResources(true, requiredOperations);
        assertEquals(86, allApiResources.size());
    }

    @Test
    void getManageableResources() {
        List<String> requiredVerbs = Arrays.asList("create", "delete", "get", "list", "patch", "update", "watch");
        List<ApiResource> apiResources = openShiftRestClient.getFilteredApiResources(true, requiredVerbs);

        List<ApiResource> manageableResources = openShiftRestClient.fetchManageableResources(openShiftRestClient.whoAmI(), "example-app-test", requiredVerbs, apiResources);

        assertEquals(86, apiResources.size());
        assertEquals(45, manageableResources.size());
    }

    @Test
    void whoAmI() {
        String s = openShiftRestClient.whoAmI();
        assertEquals("system:serviceaccount:wheel:wheel-agent-test", s);
    }

    @Test
    void fetchAllManageableResourcesInNamespace() {
        List<String> requiredVerbs = Arrays.asList("create", "delete", "get", "list", "patch", "update", "watch");
        List<ApiResource> apiResources = openShiftRestClient.getFilteredApiResources(true, requiredVerbs);
        List<ApiResource> manageableResources = openShiftRestClient.fetchManageableResources(openShiftRestClient.whoAmI(), "example-app-test", requiredVerbs, apiResources);
        List<Resource> resources = openShiftRestClient.fetchResourcesFromNamespace(manageableResources, "example-app-test");

        assertEquals(86, apiResources.size());
        assertEquals(45, manageableResources.size());
        assertEquals(27, resources.size());
    }

    @Test
    void fetchProject() {
        String projectName = "example-app-test";
        Resource project = openShiftRestClient.fetchProject(projectName);

        assertEquals("Project", project.getKind());
        assertEquals(projectName, project.getName());
        assertEquals("project.openshift.io/v1", project.getApiVersion());
    }

    @Test
    void createDelete() throws IOException {
        ObjectMapper om = new ObjectMapper();
        JsonNode secretReference = om.readTree("{ \"apiVersion\": \"v1\", \"kind\": \"Secret\", \"metadata\": { \"creationTimestamp\": \"2019-03-13T08:19:31Z\", \"name\": \"test-secret\", \"namespace\": \"example-app-test\", \"resourceVersion\": \"9861108\", \"selfLink\": \"/api/v1/namespaces/example-app-test/secrets/test-secret\", \"uid\": \"ba1ec5b4-4568-11e9-8ded-0200c0a87ac9\" }, \"type\": \"Opaque\"}");
        JsonNode delete = openShiftRestClient.delete(new Resource(null, null, secretReference));

        assertNotNull(delete);
        assertEquals("Status", delete.get("kind").textValue());
        assertEquals("Success", delete.get("status").textValue());
        assertEquals("test-secret", delete.get("details").get("name").textValue());
    }

    @Test
    void newProject() {
        String projectName = "junit-test-project";
        JsonNode newProject = openShiftRestClient.newProject(projectName);

        assertEquals("Project", newProject.get("kind").textValue());
        assertEquals(projectName, newProject.get("metadata").get("name").textValue());
    }

    @Test
    void deleteProject() {
        String projectName = "junit-test-project";
        Resource fetchProject = openShiftRestClient.fetchProject(projectName);
        openShiftRestClient.delete(fetchProject);
        Resource fetchProjectAfterDeletion = openShiftRestClient.fetchProject(projectName);
        String phase = fetchProjectAfterDeletion.getJsonNode().get("status").get("phase").textValue();
        assertEquals("Terminating", phase);
    }
}