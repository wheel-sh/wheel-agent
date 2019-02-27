package sh.wheel.gitops.agent.service;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.testutil.Samples;
import sh.wheel.gitops.agent.util.ReplaceValueStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Disabled
class ProjectResourceServiceTest {

    private OpenShiftMockServer openShiftMockServer;

    @BeforeEach
    void setUp() {
        openShiftMockServer = new OpenShiftMockServer();
    }

    @Test
    void templateCompare() throws FileNotFoundException, URISyntaxException {
        OpenShiftClient client = openShiftMockServer.createOpenShiftClient();
        ProjectResourceService projectResourceService = new ProjectResourceService(client);
        projectResourceService.setClientResources();

        Map<String, String> params = new HashMap<>();
        params.put("REPLICA_COUNT", "2");
        params.put("IMAGE_NAME", "bitnami/nginx");
        params.put("IMAGE_VERSION", "1.14-ol-7");
        Path path = Paths.get(this.getClass().getResource(Samples.TEMPLATE1.getFilePath()).toURI());
        InputStream is = ReplaceValueStream.replaceValues(new FileInputStream(path.toFile()), params);
        List<HasMetadata> items = client.templates().load(is).processLocally(params).getItems();
        client.resource(items.get(0)).inNamespace("example-app-test").get();
        projectResourceService.getNamespaceState("example-app-test");
        System.out.println();

    }

}