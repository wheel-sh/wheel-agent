package sh.wheel.gitops.agent.testutil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectStateUtil {

    public static List<JsonNode> createTestAppExampleResources() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return Files.walk(Paths.get(ProjectStateUtil.class.getResource("/samples/json/example-app-test/").toURI()))
                    .filter(f -> f.getFileName().toString().endsWith(".json"))
                    .map(Path::toFile)
                    .map(f -> {
                        try {
                            return mapper.readTree(new FileInputStream(f));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
