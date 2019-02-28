package sh.wheel.gitops.agent.testutil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectStateUtil {

    private static final ObjectReader READER = new ObjectMapper().reader();

    public static List<JsonNode> createExampleTestAppResourcesServerResponse() {
        return getJsonFilesAsJsonNodeFromDir(Samples.EXAMPLE_APP_SERVER_RESPONSE.toPath());
    }

    public static List<JsonNode> createExampleTestAppResourcesProcessed() {
        return getJsonFilesAsJsonNodeFromDir(Samples.EXAMPLE_APP_PROCESSED.toPath());
    }

    public static JsonNode getJsonAsJsonNode(Path jsonfile) {
        try {
            return READER.readTree(new FileInputStream(jsonfile.toFile()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<JsonNode> getJsonFilesAsJsonNodeFromDir(Path dir) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return Files.walk(dir)
                    .filter(f -> f.getFileName().toString().endsWith(".json"))
                    .map(ProjectStateUtil::getJsonAsJsonNode)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
