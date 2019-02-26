package sh.wheel.gitops.agent.service;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.util.ReplaceValueStream;

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
    void templateCompare() {
        OpenShiftClient client = openShiftMockServer.createOpenShiftClient();
        ProjectResourceService projectResourceService = new ProjectResourceService(client);

        Map<String, String> params = new HashMap<>();
        params.put("REPLICA_COUNT", "2");
        params.put("IMAGE_NAME", "bitnami/nginx");
        params.put("IMAGE_VERSION", "1.14-ol-7");
        List<HasMetadata> items = client.templates().load(ReplaceValueStream.replaceValues(this.getClass().getResourceAsStream("/samples/testrepo1/apps/example-app/template/app.v1.yaml"), params)).processLocally(params).getItems();

        projectResourceService.getNamespaceState("test2");

    }

}