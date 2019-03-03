package sh.wheel.gitops.agent.service;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.config.MemberConfig;
import sh.wheel.gitops.agent.config.PoolConfig;
import sh.wheel.gitops.agent.model.App;
import sh.wheel.gitops.agent.model.Group;
import sh.wheel.gitops.agent.model.WheelRepository;
import sh.wheel.gitops.agent.testutil.FileUtils;
import sh.wheel.gitops.agent.testutil.GitTestUtil;
import sh.wheel.gitops.agent.testutil.Samples;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WheelRepositoryServiceTest {

    private static Path TESTREPO1_PATH;
    private static Path REPOSITORIES_BASE_PATH;
    private WheelRepositoryService wheelRepositoryService;

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


    @BeforeEach
    void setUp() {
        wheelRepositoryService = new WheelRepositoryService();
        wheelRepositoryService.repositoryBasePath = REPOSITORIES_BASE_PATH;
    }

    @Test
    void cloneOrPullRepository() throws IOException, GitAPIException {
        // safety cleanup
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

    @Test
    void loadRepository() throws IOException, GitAPIException {
        WheelRepository wheelRepository = wheelRepositoryService.loadRepository(TESTREPO1_PATH.toString(), "master");

        Assert.assertEquals(1, wheelRepository.getApps().size());
        Assert.assertEquals(1, wheelRepository.getGroups().size());
    }


    @Test
    void readAllApps() throws IOException, URISyntaxException {
        List<App> apps = wheelRepositoryService.readAllApps(TESTREPO1_PATH.resolve("apps"));
        App app = apps.get(0);

        assertEquals("example-app", app.getAppConfig().getName());
        assertEquals("example-group", app.getAppConfig().getGroup());
        assertEquals("example-info", app.getAppConfig().getMetadata().get("custom"));
        assertNotNull(app.getBuildConfigs().get(0).getEnv());
        assertNotNull(app.getBuildConfigs().get(0).getGitUrl());
        assertNotNull(app.getBuildConfigs().get(0).getJenkinsfilePath());
        assertNotNull(app.getBuildConfigs().get(0).getName());
        assertNotNull(app.getProjectConfigs().get(0).getName());
        assertNotNull(app.getProjectConfigs().get(0).getTemplateFile());
        assertNotNull(app.getProjectConfigs().get(0).getRequests());
        assertNotNull(app.getProjectConfigs().get(0).getLimits());
        assertNotNull(app.getProjectConfigs().get(0).getParameters());
        assertNotNull(app.getProjectConfigs().get(0).getPool());
    }

    @Test
    void readAllApps_IOException_Logged() throws URISyntaxException, IOException {
        Path appsDir = Paths.get(getClass().getResource(Samples.TESTREPO2_PATH + "apps/").toURI());

        List<App> apps = wheelRepositoryService.readAllApps(appsDir);

        assertEquals(0, apps.size());
    }

    @Test
    void readAllGroups() throws IOException {
        List<Group> groups = wheelRepositoryService.readAllGroups(TESTREPO1_PATH.resolve("groups"));
        Group group = groups.get(0);
        List<PoolConfig> pools = group.getGroupConfig().getPools();
        PoolConfig pool1 = pools.get(0);
        PoolConfig pool2 = pools.get(1);
        List<MemberConfig> members = group.getMembersConfig().getMembers();
        MemberConfig member1 = members.get(0);
        MemberConfig member2 = members.get(1);

        assertEquals("example-group", group.getGroupConfig().getName());
        assertEquals(2, pools.size());
        assertEquals("production", pool1.getName());
        assertEquals("2", pool1.getRequests().getCpu());
        assertEquals("8Gi", pool1.getRequests().getMemory());
        assertEquals("3", pool1.getLimits().getCpu());
        assertEquals("10Gi", pool1.getLimits().getMemory());
        assertEquals("other", pool2.getName());
        assertEquals(3, members.size());
        assertEquals("Nik", member1.getName());
        assertEquals("nik@example.org", member1.getUserId());
        assertEquals("Admin", member1.getRole());
        assertEquals("Maxi", member2.getName());
    }

    @Test
    void missingFolders() throws URISyntaxException, IOException {
        Path testResources = Paths.get(this.getClass().getResource("/").toURI());
        Path testDir = testResources.resolve(this.getClass().getSimpleName());
        FileUtils.deleteRecursivly(testDir); // cleanup
        Files.createDirectories(testDir);

        FileNotFoundException fileNotFoundException_apps = assertThrows(FileNotFoundException.class, () -> wheelRepositoryService.getRepositoryState(testDir));
        Files.createDirectories(testDir.resolve("apps"));
        FileNotFoundException fileNotFoundException_groups = assertThrows(FileNotFoundException.class, () -> wheelRepositoryService.getRepositoryState(testDir));
        Files.createDirectories(testDir.resolve("groups"));
        FileNotFoundException fileNotFoundException_base = assertThrows(FileNotFoundException.class, () -> wheelRepositoryService.getRepositoryState(testDir));
        Path basePath = testDir.resolve("base");
        Files.createDirectories(basePath);
        FileNotFoundException fileNotFoundException_project_template = assertThrows(FileNotFoundException.class, () -> wheelRepositoryService.getRepositoryState(testDir));
        Files.createDirectories(basePath.resolve("template"));
        Files.createFile(basePath.resolve("template/project.yaml"));
        WheelRepository repositoryState = wheelRepositoryService.getRepositoryState(testDir);

        assertTrue(fileNotFoundException_apps.getMessage().contains("/apps"));
        assertTrue(fileNotFoundException_groups.getMessage().contains("/groups"));
        assertTrue(fileNotFoundException_base.getMessage().contains("/base"));
        assertTrue(fileNotFoundException_project_template.getMessage().contains("/template/project.yaml"));
        assertEquals(0, repositoryState.getApps().size());
        assertEquals(0, repositoryState.getGroups().size());
    }
}