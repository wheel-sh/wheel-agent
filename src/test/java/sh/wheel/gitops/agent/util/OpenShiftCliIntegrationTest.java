package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.testutil.Samples;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class OpenShiftCliIntegrationTest {

    private OpenShiftCli oc;

    @BeforeEach
    void setUp() {
        oc = new OpenShiftCli();
    }

    @Test
    void getResourceList() {
        JsonNode resourceList = oc.getResourceList("template", "openshift");

        assertNotNull(resourceList);
        assertEquals("List", resourceList.get("kind").textValue());
        JsonNode items = resourceList.get("items");
        assertNotNull(items);
        assertTrue(items.isArray());
    }

    @Test
    void getResource() {
        JsonNode resource = oc.getResource("template", "system", "openshift");

        assertNotNull(resource);
        assertEquals("Template", resource.get("kind").textValue());
        assertEquals("system", resource.get("metadata").get("name").textValue());
    }

    @Test
    void getAllApiResources() {
        List<String> allApiResources = oc.getAllApiResources();
        assertTrue(allApiResources.size() > 100);
    }

    @Test
    void getApiResourcesNamespaced_True() {
        List<String> apiResources = oc.getApiResources(true);
        assertTrue(apiResources.contains("configmaps"));
        assertFalse(apiResources.contains("namespaces"));
    }

    @Test
    void getApiResourcesNamespaced_False() {
        List<String> apiResources = oc.getApiResources(false);
        assertTrue(apiResources.contains("namespaces"));
        assertFalse(apiResources.contains("configmaps"));
    }

    @Test
    void getApiResourcesWideNamespaced_True() {
        List<String> apiResources = oc.getApiResourcesWide(true);
        assertTrue(apiResources.size() > 50);
    }

    @Test
    void process() throws FileNotFoundException {
        Map<String, String> params = new HashMap<>();
        params.put("REPLICA_COUNT", "2");
        params.put("IMAGE_NAME", "bitnami/nginx");
        params.put("IMAGE_VERSION", "1.14-ol-7");
        JsonNode process = oc.process(Samples.TEMPLATE1.toPath().toAbsolutePath().toString(), params);
//
//        try (PrintStream out = new PrintStream(new FileOutputStream("example-app-processed.json"))) {
//            out.print(process);
//        }
    }

    @Test
    @Disabled
    void getAllNamespacedResource() throws FileNotFoundException {
        String project = "example-app-test";
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
        for (JsonNode projectResource : projectResources) {
            JsonNode items = projectResource.get("items");
            if (items != null && items.size() > 0) {
                String kind = items.get(0).get("kind").textValue();
                try (PrintStream out = new PrintStream(new FileOutputStream(kind + ".json"))) {
                    out.print(projectResource);
                }
            }
        }
    }

    @Test
    void whoami() {
        String whoami = oc.getWhoAmI();
        assertNotNull(whoami);
    }

    @Test
    void canI() {
        boolean b = oc.canI("get", "pods");
        assertTrue(b);
    }
}