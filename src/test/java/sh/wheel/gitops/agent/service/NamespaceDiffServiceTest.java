package sh.wheel.gitops.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftMockServer;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.testutil.ProjectStateUtil;
import sh.wheel.gitops.agent.testutil.Samples;
import sh.wheel.gitops.agent.util.ReplaceValueStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

class NamespaceDiffServiceTest {

    private OpenShiftServer openShiftServer;

    @BeforeEach
    void setUp() {
        openShiftServer = new OpenShiftServer();
        openShiftServer.before();
    }

    @AfterEach
    void tearDown() {
        openShiftServer.after();
    }

    @Test
    void test() {
        List<JsonNode> testAppExampleResources = ProjectStateUtil.createTestAppExampleResources();
        System.out.println();
    }

    @Test
    void compare() {
        OpenShiftClient client = openShiftServer.getOpenshiftClient();
        Map<String, String> params = new HashMap<>();
        params.put("REPLICA_COUNT", "2");
        params.put("IMAGE_NAME", "bitnami/nginx");
        params.put("IMAGE_VERSION", "1.14-ol-7");
        List<HasMetadata> templateResources = client.templates().load(ReplaceValueStream.replaceValues(this.getClass().getResourceAsStream(Samples.TEMPLATE1.getFilePath()), params)).processLocally(params).getItems();
        ProjectResourceService projectResourceService = new ProjectResourceService(client);
        List<HasMetadata> projectResources = projectResourceService.getNamespaceState("test2").getResources();
//
//        HasMetadata templateService = templateResources.stream().filter(tr -> tr.getKind().equals("Service")).findFirst().getResourceList();
//        HasMetadata projectService = projectResources.getResourceList("Service").getResourceList(0);
//
//        HasMetadata templateDeploymentConfig = templateResources.stream().filter(tr -> tr.getKind().equals("DeploymentConfig")).findFirst().getResourceList();
//        HasMetadata projectDeploymentConfig = projectResources.getResourceList("DeploymentConfig").getResourceList(0);
//
//        HasMetadata templateRoute = templateResources.stream().filter(tr -> tr.getKind().equals("Route")).findFirst().getResourceList();
//        HasMetadata projectRoute = projectResources.getResourceList("Route").getResourceList(0);
//
//
//        boolean serviceChanged = new ResourceDifferenceEvaluator().evaluateDiff(templateService, projectService).size() > 0;
//        boolean dcChanged = new ResourceDifferenceEvaluator().evaluateDiff(templateDeploymentConfig, projectDeploymentConfig).size() > 0;
//        boolean routeChanged = new ResourceDifferenceEvaluator().evaluateDiff(templateRoute, projectRoute).size() > 0;
//
//        assertFalse(serviceChanged);
//        assertFalse(dcChanged);
//        assertFalse(routeChanged);

    }

}