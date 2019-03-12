package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.model.ApiResource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenShiftRestClientIntegrationTest {

    private static String token;
    private static String apiServerUrl;
    private OpenShiftRestClient openShiftRestClient;

    @BeforeAll
    public static void beforeAll() {
        apiServerUrl = System.getProperty("api.server");
        token = System.getProperty("api.server.token");
    }

    @BeforeEach
    void setUp() {
        openShiftRestClient = OpenShiftRestClient.create(apiServerUrl, token);
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
        for (int i = 0; i < 5; i++) {
            long start = System.currentTimeMillis();
            List<ApiResource> manageableResources = openShiftRestClient.getManageableResources(openShiftRestClient.whoAmI(), "example-app-test", requiredVerbs);
            System.out.println("Millis: " + (System.currentTimeMillis() - start));
            assertNotNull(manageableResources);
        }
    }

    @Test
    void whoAmI() {
        String s = openShiftRestClient.whoAmI();
        assertNotNull(s);
    }
}