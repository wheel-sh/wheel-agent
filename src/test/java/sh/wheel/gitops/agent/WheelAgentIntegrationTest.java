package sh.wheel.gitops.agent;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class WheelAgentIntegrationTest {

    @Test
    void start() throws URISyntaxException, IOException {
        long start = System.nanoTime();
        Path repositoryPath = Paths.get(this.getClass().getResource("/samples/testrepo1").toURI());
        new WheelAgent().start(repositoryPath);
        System.out.println(System.nanoTime() - start);
    }
}