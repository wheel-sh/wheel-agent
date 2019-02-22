package sh.wheel.gitops.agent.service;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.utils.ReplaceValueStream;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProjectResourceServiceIntegrationTest {

    @Test
    void templateCompare() {
        OpenShiftClient client = new DefaultOpenShiftClient();
        ProjectResourceService projectResourceService = new ProjectResourceService(client);

        Map<String, String> params = new HashMap<>();
        params.put("REPLICA_COUNT", "2");
        params.put("IMAGE_NAME", "bitnami/nginx");
        params.put("IMAGE_VERSION", "1.14-ol-7");
        List<HasMetadata> items = client.templates().load(ReplaceValueStream.replaceValues(this.getClass().getResourceAsStream("/samples/testrepo1/apps/example-app/template/app.v1.yaml"), params)).processLocally(params).getItems();

        projectResourceService.getNamespaceState("test2");

    }

}