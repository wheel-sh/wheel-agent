package sh.wheel.gitops.agent.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.model.*;
import sh.wheel.gitops.agent.testutil.OpenShiftServiceTestUtil;
import sh.wheel.gitops.agent.testutil.Samples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ProjectDifferenceServiceTest {

    private OpenShiftService openShiftService;
    private ProjectDifferenceService projectDifferenceService;

    @BeforeEach
    void setUp() {
        openShiftService = OpenShiftServiceTestUtil.createWithMockData(Samples.MOCK_DATA1.toPath());
        projectDifferenceService = new ProjectDifferenceService();
    }

    @Test
    void compare() {
        ProjectState allNamespacedResourcesTestData = getAllNamespacedResourcesTestData();
        ProjectState processedState = processTestData();
        Map<ResourceKey, Resource> processDeloymentConfig = processedState.getResourcesByKey().entrySet().stream().filter(e -> e.getKey().getKind().equals("DeploymentConfig")).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<ResourceKey, Resource> projectDeloymentConfig = allNamespacedResourcesTestData.getResourcesByKey().entrySet().stream().filter(e -> e.getKey().getKind().equals("DeploymentConfig")).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        ProjectState processedProjectState = new ProjectState(new Resource(ResourceKey.projectWithName("example-app-test"), null, null), processDeloymentConfig);
        ProjectState projectProjectState = new ProjectState(new Resource(ResourceKey.projectWithName("example-app-test"), null, null), projectDeloymentConfig);

        List<ResourceDifference> resourceDifferences = projectDifferenceService.evaluateDifference(processedProjectState, projectProjectState);

        Assertions.assertEquals(1, resourceDifferences.size());
        ResourceDifference difference = resourceDifferences.get(0);
        Assertions.assertEquals(ResourcePresence.BOTH, difference.getPresence());

    }

    private ProjectState getAllNamespacedResourcesTestData() {
        return openShiftService.getProjectStateFromCluster("example-app-test");
    }


    private ProjectState processTestData() {
        Map<String, String> appParams = new HashMap<>();
        appParams.put("REPLICA_COUNT", "2");
        appParams.put("IMAGE_NAME", "bitnami/nginx");
        appParams.put("IMAGE_VERSION", "1.14-ol-7");

        Map<String, String> projectParams = new HashMap<>();
        appParams.put("PROJECT_NAME", "example-app-test");
        appParams.put("PROJECT_REQUESTING_USER", "admin@nikio.io");
        appParams.put("PROJECT_ADMIN_USER", "admin@nikio.io");
        return openShiftService.getProjectStateFromTemplate(Samples.BASE_PROJECT_TEMPLATE.toPath(), projectParams, Samples.TEMPLATE1.toPath(), appParams);
    }

}