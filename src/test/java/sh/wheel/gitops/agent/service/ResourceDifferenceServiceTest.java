package sh.wheel.gitops.agent.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.model.*;
import sh.wheel.gitops.agent.testutil.OpenShiftCliMockUtil;
import sh.wheel.gitops.agent.testutil.Samples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDifferenceServiceTest {
    private List<ResourceDifference> resourceDifferences;
    private ProjectState processedProjectState;
    private ProjectState clusterProjectState;

    @BeforeEach
    void setUp() {
        OpenShiftService openShiftService = new OpenShiftService(OpenShiftCliMockUtil.createOpenShiftCliMock());
        NamespaceDiffService namespaceDiffService = new NamespaceDiffService();
        clusterProjectState = openShiftService.getProjectStateFromCluster("example-app-test");

        Map<String, String> appParams = new HashMap<>();
        appParams.put("REPLICA_COUNT", "2");
        appParams.put("IMAGE_NAME", "bitnami/nginx");
        appParams.put("IMAGE_VERSION", "1.14-ol-7");

        Map<String, String> projectParams = new HashMap<>();
        appParams.put("PROJECT_NAME", "example-app-test");
        appParams.put("PROJECT_REQUESTING_USER", "admin@nikio.io");
        appParams.put("PROJECT_ADMIN_USER", "admin@nikio.io");
        processedProjectState = openShiftService.getProjectStateFromTemplate(Samples.PROJECT_TEMPLATE.toPath(), projectParams, Samples.TEMPLATE1.toPath(), appParams);

        resourceDifferences = namespaceDiffService.evaluateDifference(processedProjectState, clusterProjectState);
    }

    @Test
    void createResourceActions() {
        List<ResourceAction> resourceActions = new ResourceDifferenceService().createResourceActions(resourceDifferences, processedProjectState, clusterProjectState);

        Map<ActionType, List<ResourceAction>> byType = resourceActions.stream().collect(Collectors.groupingBy(ResourceAction::getType));
        assertEquals(9, resourceActions.size());
        assertEquals(1, byType.get(ActionType.APPLY).size());
        assertEquals(2, byType.get(ActionType.DELETE).size());
        assertEquals(4, byType.get(ActionType.WARNING).size());
        assertEquals(2, byType.get(ActionType.IGNORE).size());
    }



}