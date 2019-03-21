package sh.wheel.gitops.agent;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sh.wheel.gitops.agent.testutil.GitTestUtil;
import sh.wheel.gitops.agent.testutil.Samples;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class TestRunner {

    public static void main(String[] args) throws Exception {
        Path TESTREPO1_PATH = Paths.get(TestRunner.class.getResource(Samples.TESTREPO1_PATH).toURI());
        URI resourceBasePath = TestRunner.class.getResource("/").toURI();
        Path REPOSITORIES_BASE_PATH = Paths.get(resourceBasePath).resolve("wheel-test-repos");
        GitTestUtil.initGitRepoIfNotExists(TESTREPO1_PATH);
        Files.createDirectories(REPOSITORIES_BASE_PATH);

        System.setProperty("CONFIG_REPOSITORY_URL", Samples.TESTREPO1.toPath().toAbsolutePath().toString());
        System.setProperty("CONFIG_REPOSITORY_BRANCH", "master");
        System.setProperty("CHECKOUT_BASE_PATH", REPOSITORIES_BASE_PATH.toAbsolutePath().toString());

        SpringApplication.run(TestRunner.class, args);
    }



}
