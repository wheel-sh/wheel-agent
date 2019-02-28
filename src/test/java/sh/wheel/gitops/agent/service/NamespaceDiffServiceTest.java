package sh.wheel.gitops.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.testutil.ProjectStateUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Disabled
class NamespaceDiffServiceTest {


    @Test
    void test() {

        List<JsonNode> serverResponse = ProjectStateUtil.createExampleTestAppResourcesServerResponse();
        List<JsonNode> processed = ProjectStateUtil.createExampleTestAppResourcesProcessed();

        assertEquals(10, serverResponse.size());
        assertEquals(3, processed.size());

    }

    @Test
    void compare() {

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