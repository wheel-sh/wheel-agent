package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class OpenShiftCliIntegrationTest {

    @Test
    void getResourceList() {
        JsonNode resourceList = new OpenShiftCli().getResourceList("pods", "default");

        assertNotNull(resourceList);
        assertEquals("List", resourceList.get("kind").textValue());
        JsonNode items = resourceList.get("items");
        assertNotNull(items);
        assertTrue(items.isArray());
    }

    @Test
    void getResource() {
        JsonNode resource = new OpenShiftCli().getResource("deploymentconfig", "example-app", "example-app-test");

        assertNotNull(resource);
        assertEquals("DeploymentConfig", resource.get("kind").textValue());
        assertEquals(1, resource.get("spec").get("replicas").intValue());
    }

    @Test
    void getAllApiResources() {
        List<String> allApiResources = new OpenShiftCli().getAllApiResources();
        assertTrue(allApiResources.size() > 100);
    }

    @Test
    void getApiResourcesNamespaced_True() {
        List<String> apiResources = new OpenShiftCli().getApiResources(true);
        assertTrue(apiResources.contains("configmaps"));
        assertFalse(apiResources.contains("namespaces"));
    }

    @Test
    void getApiResourcesNamespaced_False() {
        List<String> apiResources = new OpenShiftCli().getApiResources(false);
        assertTrue(apiResources.contains("namespaces"));
        assertFalse(apiResources.contains("configmaps"));
    }

    @Test
    void process() {

    }

    @Test
    @Disabled
    void getAllNamespacedResource() throws FileNotFoundException {
        String project = "example-app-test";
        OpenShiftCli oc = new OpenShiftCli();

        final AtomicLong max = new AtomicLong();
        long start = System.nanoTime();
        List<String> apiResources = oc.getApiResources(true).stream().filter(ar -> !ar.endsWith("security.openshift.io")).collect(Collectors.toList());
        List<JsonNode> projectResources = apiResources.stream().parallel().map(ar -> {
            try {
                long reqStart = System.nanoTime();
                JsonNode resourceList = oc.getResourceList(ar, project);
                long reqTime = System.nanoTime() - reqStart;
                if (reqTime > max.longValue()) {
                    max.set(reqTime);
                    System.out.println("New Max time: " + reqTime + " - " + ar);
                }
                return resourceList;
            } catch (MethodNotAllowedException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull)
                .collect(Collectors.toList());
        System.out.println(System.nanoTime() - start);
//        for (JsonNode projectResource : projectResources) {
//            JsonNode items = projectResource.get("items");
//            if(items != null && items.size() > 0) {
//                String kind = items.get(0).get("kind").textValue();
//                try (PrintStream out = new PrintStream(new FileOutputStream(kind+ ".json"))) {
//                    out.print(items);
//                }
//            }
//        }
    }
}