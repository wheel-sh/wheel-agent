package sh.wheel.gitops.agent.service;

import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.config.AppConfig;
import sh.wheel.gitops.agent.model.App;
import sh.wheel.gitops.agent.model.Group;
import sh.wheel.gitops.agent.model.NamespaceState;
import sh.wheel.gitops.agent.model.WheelRepository;
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
    private WheelRepositoryService wheelRepositoryService;

    @BeforeAll
    static void initRepo() throws GitAPIException, URISyntaxException, IOException {
        TESTREPO1_PATH = Paths.get(WheelRepositoryServiceTest.class.getResource(Samples.TESTREPO1_PATH).toURI());
    }

    @Test
    void processExpectedNamespaceStatesByName() throws IOException, GitAPIException {
        OpenShiftClient client = new DefaultOpenShiftClient();
        WheelRepository wheelRepository = new WheelRepositoryService().getRepositoryState(TESTREPO1_PATH);
        ConfigProcessingService configProcessingService = new ConfigProcessingService(client);

        List<NamespaceState> nsState = configProcessingService.processExpectedNamespaceStatesByName(wheelRepository);

        assertNotNull(nsState);
        assertEquals(1, nsState.size());
        NamespaceState namespaceState = nsState.get(0);
        assertEquals(3, namespaceState.getResources().size());
    }

    @Test
    void processExpectedNamespaceStatesByName_FaultApp() {
        Map<String, App> apps = new HashMap<>();
        AppConfig appConfig = new AppConfig();
        appConfig.setName("app");
        apps.put("app", new App(appConfig, new ArrayList<>(), new ArrayList<>(), null));
        WheelRepository wheelRepository = new WheelRepository(apps, new HashMap<>());

        List<NamespaceState> namespaceStates = new ConfigProcessingService(null).processExpectedNamespaceStatesByName(wheelRepository);

        Assert.assertEquals(0, namespaceStates.size());
    }
}