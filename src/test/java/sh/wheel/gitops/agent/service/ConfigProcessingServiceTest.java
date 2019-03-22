package sh.wheel.gitops.agent.service;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.config.AppConfig;
import sh.wheel.gitops.agent.model.*;
import sh.wheel.gitops.agent.testutil.OpenShiftServiceTestUtil;
import sh.wheel.gitops.agent.testutil.Samples;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigProcessingServiceTest {

    private static Path TESTREPO1_PATH;
    private ConfigProcessingService configProcessingService;
    private WheelRepository wheelRepository;

    @BeforeAll
    static void initRepo() throws  URISyntaxException {
        TESTREPO1_PATH = Paths.get(WheelRepositoryServiceTest.class.getResource(Samples.TESTREPO1_PATH).toURI());
    }

    @BeforeEach
    void setUp() throws IOException {
        wheelRepository = new WheelRepositoryService().getRepositoryState(TESTREPO1_PATH);
        OpenShiftService openShiftService = OpenShiftServiceTestUtil.createWithMockData(Samples.MOCK_DATA1.toPath());
        configProcessingService = new ConfigProcessingService(openShiftService);
    }

    @Test
    void processExpectedProjectStates() {
        List<ProjectState> projectStates = configProcessingService.processExpectedProjectStates(wheelRepository);

        assertNotNull(projectStates);
        assertEquals(1, projectStates.size());
        ProjectState projectState = projectStates.get(0);
        assertEquals(7, projectState.getResourcesByKey().size());
    }

    @Test
    void processExpectedNamespaceStates_FaultApp() {
        Map<String, App> apps = new HashMap<>();
        AppConfig appConfig = new AppConfig();
        appConfig.setGroup("group");
        apps.put("app", new App("app", appConfig, new ArrayList<>(), new HashMap<>(), null));
        HashMap<String, Group> groups = new HashMap<>();
        Group group = new Group(null, null);
        groups.put("group", group);
        WheelRepository wheelRepository = new WheelRepository(apps, groups, new BaseConfig(Samples.BASE_PROJECT_TEMPLATE.toPath()));

        List<ProjectState> projectStates = configProcessingService.processExpectedProjectStates(wheelRepository);

        Assert.assertEquals(0, projectStates.size());
    }
}