package sh.wheel.gitops.agent.service;

import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.model.App;
import sh.wheel.gitops.agent.testutil.Samples;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryConfigServiceTest {

    @Test
    void readAllApps() throws IOException, URISyntaxException {
        Path appsDir = Paths.get(getClass().getResource(Samples.TESTREPO1_PATH + "apps/").toURI());
        RepositoryConfigService repositoryConfigService = new RepositoryConfigService();

        List<App> apps = repositoryConfigService.readAllApps(appsDir);
        App app = apps.get(0);

        assertEquals("example-app", app.getAppConfig().getName());
        assertEquals("example-group", app.getAppConfig().getGroup());
        assertEquals("example-info", app.getAppConfig().getMetadata().get("custom"));
        assertNotNull(app.getBuildConfigs().get(0).getEnv());
        assertNotNull(app.getBuildConfigs().get(0).getGitUrl());
        assertNotNull(app.getBuildConfigs().get(0).getJenkinsfilePath());
        assertNotNull(app.getBuildConfigs().get(0).getName());
        assertNotNull(app.getNamespaceConfigs().get(0).getName());
        assertNotNull(app.getNamespaceConfigs().get(0).getTemplateFile());
        assertNotNull(app.getNamespaceConfigs().get(0).getRequests());
        assertNotNull(app.getNamespaceConfigs().get(0).getLimits());
        assertNotNull(app.getNamespaceConfigs().get(0).getParameters());
        assertNotNull(app.getNamespaceConfigs().get(0).getPool());
    }

    @Test
    void readAllApps_IOException_Logged() throws URISyntaxException, IOException {
        Path appsDir = Paths.get(getClass().getResource(Samples.TESTREPO2_PATH + "apps/").toURI());
        RepositoryConfigService repositoryConfigService = new RepositoryConfigService();

        List<App> apps = repositoryConfigService.readAllApps(appsDir);

        assertEquals(0, apps.size());
    }

}