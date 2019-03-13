package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.model.ApiResource;
import sh.wheel.gitops.agent.model.Resource;
import sh.wheel.gitops.agent.model.ResourceKey;

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
            List<ApiResource> manageableResources = openShiftRestClient.fetchManageableResources(openShiftRestClient.whoAmI(), "example-app-test", requiredVerbs, apiResources);
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
        ApiResource dcv1beta1 = ApiResource.newBuilder()
                .name("deployments")
                .kind("Deployment")
                .groupName("extensions")
                .groupVersion("extensions/v1beta1")
                .apiVersion("v1beta1")
                .coreApi(false)
                .subresource(false)
                .namespaced(true)
                .build();
        ApiResource dcv1beta2 = ApiResource.newBuilder()
                .name("deployments")
                .kind("Deployment")
                .groupName("apps")
                .groupVersion("apps/v1beta2")
                .apiVersion("v1beta2")
                .coreApi(false)
                .subresource(false)
                .namespaced(true)
                .build();
        ApiResource dcv1 = ApiResource.newBuilder()
                .name("deployments")
                .kind("Deployment")
                .groupName("apps")
                .groupVersion("apps/v1")
                .apiVersion("v1")
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
        ResourceKey project = ResourceKey.PROJECT;
        String namespace = "example-app-test";
        Resource r = openShiftRestClient.fetchResource(ResourceKey.PROJECT, namespace);

        assertNotNull(r);
    }
}