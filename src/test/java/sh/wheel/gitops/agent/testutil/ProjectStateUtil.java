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
        return getJsonFilesAsJsonNodeFromDir(Samples.EXAMPLE_APP_SERVER_RESPONSE_DIR.toPath());

    }

    public static JsonNode createExampleTestAppResourcesProcessed() {
        try {
            return READER.readTree(new FileInputStream(Samples.EXAMPLE_APP_PROCESSED_FILE.toPath().toFile()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
            return Files.walk(dir)
                    .filter(f -> f.getFileName().toString().endsWith(".json"))
                    .map(ProjectStateUtil::getJsonAsJsonNode)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode createExampleTestAppProjectProcessed() {
        try {
            return READER.readTree(new FileInputStream(Samples.EXAMPLE_APP_PROJECT_PROCESSED_FILE.toPath().toFile()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static JsonNode createExampleTestAppProjectServerResponse() {
        try {
            return READER.readTree(new FileInputStream(Samples.EXAMPLE_APP_PROJECT_SERVER_RESPONSE.toPath().toFile()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
