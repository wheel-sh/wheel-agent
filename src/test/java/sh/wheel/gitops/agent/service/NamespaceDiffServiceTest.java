package sh.wheel.gitops.agent.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.model.DifferenceType;
import sh.wheel.gitops.agent.model.ProjectState;
import sh.wheel.gitops.agent.model.Resource;
import sh.wheel.gitops.agent.model.ResourceDifference;
import sh.wheel.gitops.agent.testutil.OpenShiftCliMockUtil;
import sh.wheel.gitops.agent.testutil.Samples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class NamespaceDiffServiceTest {

    private OpenShiftService openShiftService;
    private NamespaceDiffService namespaceDiffService;

    @BeforeEach
    void setUp() {
        openShiftService = new OpenShiftService(OpenShiftCliMockUtil.createOpenShiftCliMock());
        namespaceDiffService = new NamespaceDiffService();
    }

    @Test
    void compare() {
        ProjectState allNamespacedResourcesTestData = getAllNamespacedResourcesTestData();
        Map<String, List<Resource>> processTestData = processTestData();
        Map<String, List<Resource>> processDeloymentConfig = allNamespacedResourcesTestData.getResourcesByKind().entrySet().stream().filter(e -> e.getKey().equals("DeploymentConfig")).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, List<Resource>> projectDeloymentConfig = processTestData.entrySet().stream().filter(e -> e.getKey().equals("DeploymentConfig")).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        ProjectState processedProjectState = new ProjectState("example-app-test", processDeloymentConfig);
        ProjectState projectProjectState = new ProjectState("example-app-test", projectDeloymentConfig);

        List<ResourceDifference> resourceDifferences = namespaceDiffService.evaluateDifference(processedProjectState, projectProjectState);

        Assertions.assertEquals(1, resourceDifferences.size());
        ResourceDifference difference = resourceDifferences.get(0);
        Assertions.assertEquals(DifferenceType.DIFFER, difference.getType());

    }

    private ProjectState getAllNamespacedResourcesTestData() {
        return openShiftService.getProjectStateFromCluster("example-app-test");
    }


    private Map<String, List<Resource>> processTestData() {
        Map<String, String> params = new HashMap<>();
        params.put("REPLICA_COUNT", "2");
        params.put("IMAGE_NAME", "bitnami/nginx");
        params.put("IMAGE_VERSION", "1.14-ol-7");

        Map<String, List<Resource>> process = openShiftService.process(Samples.TEMPLATE1.toPath(), params);
        return process;
    }

}