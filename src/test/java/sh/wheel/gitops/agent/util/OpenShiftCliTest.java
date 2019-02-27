package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class OpenShiftCliTest {

    @Test
    void get() throws IOException {
        JsonNode x = OpenShiftCli.get("all", "default");
        System.out.println(x.get("kind"));
        JsonNode items = x.get("items");
        for (JsonNode item : items) {
            System.out.println(item.get("kind"));
        }
    }

}