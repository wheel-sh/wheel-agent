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
        apiServerUrl = System.getenv("OPENSHIFT_API_SERVER");
        token = System.getenv("OPENSHIFT_API_TOKEN");
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
            List<ApiResource> apiResources = openShiftRestClient.getFilteredApiResources(true, requiredVerbs);
            List<ApiResource> manageableResources = openShiftRestClient.getManageableResources(openShiftRestClient.whoAmI(), "example-app-test", requiredVerbs, apiResources);
            System.out.println("Millis: " + (System.currentTimeMillis() - start));
            assertNotNull(manageableResources);
        }
    }

    @Test
    void whoAmI() {
        String s = openShiftRestClient.whoAmI();
        assertNotNull(s);
    }

    @Test
    void fetchResources() {
//        ApiResource{name='deploymentconfigs', subresource=false, kind='DeploymentConfig', apiGroup='apps.openshift.io', apiVersion='v1', namespaced=true, verbs=[create, delete, deletecollection, get, list, patch, update, watch]}
    }
}