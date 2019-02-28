package sh.wheel.gitops.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.testutil.Samples;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenShiftServiceIntegrationTest {

    private OpenShiftService openShiftService;

    @BeforeEach
    void setUp() {
        openShiftService = new OpenShiftService();
    }

    @Test
    void getAllNamespacedResources() {
        List<JsonNode> allNamespacedResources = openShiftService.getAllNamespacedResources("example-app-test");

        assertTrue(allNamespacedResources.size() > 5);
    }

    @Test
    void process() {
        Map<String, String> params = new HashMap<>();
        params.put("REPLICA_COUNT", "2");
        params.put("IMAGE_NAME", "bitnami/nginx");
        params.put("IMAGE_VERSION", "1.14-ol-7");

        List<JsonNode> process = openShiftService.process(Samples.TEMPLATE1.toPath(), params);

        assertTrue(process.size() > 2);

        for (JsonNode projectResource : process) {
            String kind = projectResource.get("kind").textValue();
            try (PrintStream out = new PrintStream(new FileOutputStream(kind + ".json"))) {
                out.print(projectResource);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}