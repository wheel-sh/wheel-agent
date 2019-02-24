package sh.wheel.gitops.agent.service;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.testutil.FileUtils;
import sh.wheel.gitops.agent.testutil.GitTestUtil;
import sh.wheel.gitops.agent.testutil.Samples;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class WheelRepositoryServiceTest {

    private static Path TESTREPO1_PATH;
    private static Path REPOSITORIES_BASE_PATH;

    @BeforeAll
    static void initRepo() throws GitAPIException, URISyntaxException, IOException {
        TESTREPO1_PATH = Paths.get(WheelRepositoryServiceTest.class.getResource(Samples.TESTREPO1_PATH).toURI());
        GitTestUtil.initGitRepoIfNotExists(TESTREPO1_PATH);
        URI resourceBasePath = WheelRepositoryServiceTest.class.getResource("/").toURI();
        REPOSITORIES_BASE_PATH = Paths.get(resourceBasePath).resolve("wheel-test-repos");
        Files.createDirectories(REPOSITORIES_BASE_PATH);
    }

    @AfterAll
    static void cleanUp() throws IOException {
        FileUtils.deleteRecursivly(TESTREPO1_PATH.resolve(".git"));
        FileUtils.deleteRecursivly(REPOSITORIES_BASE_PATH);
    }


    @Test
    void cloneOrPullRepository() throws IOException, GitAPIException {
        WheelRepositoryService wheelRepositoryService = new WheelRepositoryService();
        wheelRepositoryService.repositoryBasePath = REPOSITORIES_BASE_PATH;
        Path expectedRepositoryPath = REPOSITORIES_BASE_PATH.resolve("testrepo1");
        FileUtils.deleteRecursivly(expectedRepositoryPath);

        wheelRepositoryService.cloneOrPullRepository(TESTREPO1_PATH.toString(), "master");
        String initialHead = GitTestUtil.getHeadId(expectedRepositoryPath);
        GitTestUtil.addEmptyCommit(TESTREPO1_PATH, "Test commit");
        wheelRepositoryService.cloneOrPullRepository(TESTREPO1_PATH.toString(), "master");
        String secondHead = GitTestUtil.getHeadId(expectedRepositoryPath);

        Assert.assertTrue(Files.exists(expectedRepositoryPath));
        Assert.assertNotEquals(initialHead, secondHead);
    }
}