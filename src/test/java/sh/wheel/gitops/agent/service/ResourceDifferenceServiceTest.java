package sh.wheel.gitops.agent.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.model.ActionType;
import sh.wheel.gitops.agent.model.ProjectState;
import sh.wheel.gitops.agent.model.ResourceAction;
import sh.wheel.gitops.agent.model.ResourceDifference;
import sh.wheel.gitops.agent.testutil.OpenShiftServiceTestUtil;
import sh.wheel.gitops.agent.testutil.Samples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceDifferenceServiceTest {
    private List<ResourceDifference> resourceDifferences;
    private ProjectState processedProjectState;
    private ProjectState clusterProjectState;

    @BeforeEach
    void setUp() {
        OpenShiftService openShiftService = OpenShiftServiceTestUtil.createWithMockData(Samples.MOCK_DATA1.toPath());
        ProjectDifferenceService projectDifferenceService = new ProjectDifferenceService();
        clusterProjectState = openShiftService.getProjectStateFromCluster("example-app-test");

        Map<String, String> appParams = new HashMap<>();
        appParams.put("REPLICA_COUNT", "2");
        appParams.put("IMAGE_NAME", "bitnami/nginx");
        appParams.put("IMAGE_VERSION", "1.14-ol-7");

        Map<String, String> projectParams = new HashMap<>();
        projectParams.put("PROJECT_NAME", "example-app-test");
        projectParams.put("PROJECT_REQUESTING_USER", "admin@nikio.io");
        projectParams.put("PROJECT_ADMIN_USER", "admin@nikio.io");
        processedProjectState = openShiftService.getProjectStateFromTemplate(Samples.BASE_PROJECT_TEMPLATE.toPath(), projectParams, Samples.TEMPLATE1.toPath(), appParams);

        resourceDifferences = projectDifferenceService.evaluateDifference(processedProjectState, clusterProjectState);
    }

    @Test
    void createResourceActions() {
        List<ResourceAction> resourceActions = new ResourceDifferenceService().createResourceActions(resourceDifferences, processedProjectState, clusterProjectState);

        Map<ActionType, List<ResourceAction>> byType = resourceActions.stream().collect(Collectors.groupingBy(ResourceAction::getType));
        assertEquals(10, resourceActions.size());
        assertEquals(1, byType.get(ActionType.PATCH).size());
        assertEquals(3, byType.get(ActionType.IGNORE_OWNED_RESOURCE).size());
        assertEquals(6, byType.get(ActionType.IGNORE_CLUSTER_ATTR).size());
    }

}