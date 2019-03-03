package sh.wheel.gitops.agent.service;

import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import sh.wheel.gitops.agent.config.AppConfig;
import sh.wheel.gitops.agent.model.App;
import sh.wheel.gitops.agent.model.BaseConfig;
import sh.wheel.gitops.agent.model.ProjectState;
import sh.wheel.gitops.agent.model.WheelRepository;
import sh.wheel.gitops.agent.testutil.OpenShiftCliMockUtil;
import sh.wheel.gitops.agent.testutil.Samples;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigProcessingServiceTest {

    private static Path TESTREPO1_PATH;
    private ConfigProcessingService configProcessingService;
    private WheelRepository wheelRepository;

    @BeforeAll
    static void initRepo() throws GitAPIException, URISyntaxException, IOException {
        TESTREPO1_PATH = Paths.get(WheelRepositoryServiceTest.class.getResource(Samples.TESTREPO1_PATH).toURI());
    }

    @BeforeEach
    void setUp() throws IOException {
        wheelRepository = new WheelRepositoryService().getRepositoryState(TESTREPO1_PATH);
        OpenShiftService openShiftService = new OpenShiftService(OpenShiftCliMockUtil.createOpenShiftCliMock());
        configProcessingService = new ConfigProcessingService(openShiftService);
    }

    @Test
    void processExpectedProjectStates() throws IOException, GitAPIException {
        List<ProjectState> projectStates = configProcessingService.processExpectedProjectStates(wheelRepository);

        assertNotNull(projectStates);
        assertEquals(1, projectStates.size());
        ProjectState projectState = projectStates.get(0);
        assertEquals(5, projectState.getResourcesByKind().size());
    }

    @Test
    void processExpectedNamespaceStates_FaultApp() {
        Map<String, App> apps = new HashMap<>();
        AppConfig appConfig = new AppConfig();
        appConfig.setName("app");
        apps.put("app", new App(appConfig, new ArrayList<>(), new ArrayList<>(), null));
        WheelRepository wheelRepository = new WheelRepository(apps, new HashMap<>(), new BaseConfig(Samples.BASE_PROJECT_TEMPLATE.toPath()));

        List<ProjectState> projectStates = configProcessingService.processExpectedProjectStates(wheelRepository);

        Assert.assertEquals(0, projectStates.size());
    }
}