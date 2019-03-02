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
    private NamespaceState processedNamespaceState;
    private NamespaceState projectNamespaceState;

    @BeforeEach
    void setUp() {
        OpenShiftService openShiftService = new OpenShiftService(OpenShiftCliMockUtil.createOpenShiftCliMock());
        NamespaceDiffService namespaceDiffService = new NamespaceDiffService();
        Map<String, List<Resource>> allNamespacedResources = openShiftService.getAllNamespacedResources("example-app-test");
        Map<String, String> params = new HashMap<>();
        params.put("REPLICA_COUNT", "2");
        params.put("IMAGE_NAME", "bitnami/nginx");
        params.put("IMAGE_VERSION", "1.14-ol-7");
        Map<String, List<Resource>> process = openShiftService.process(Samples.TEMPLATE1.toPath(), params);
        processedNamespaceState = new NamespaceState("example-app-test", process);
        projectNamespaceState = new NamespaceState("example-app-test", allNamespacedResources);
        resourceDifferences = namespaceDiffService.evaluateDifference(processedNamespaceState, projectNamespaceState);
    }

    @Test
    void createResourceActions() {
        List<ResourceAction> resourceActions = new ResourceDifferenceService().createResourceActions(resourceDifferences, processedNamespaceState, projectNamespaceState);

        Map<ActionType, List<ResourceAction>> byType = resourceActions.stream().collect(Collectors.groupingBy(ResourceAction::getType));
        assertEquals(7, resourceActions.size());
        assertEquals(1, byType.get(ActionType.APPLY).size());
        assertEquals(2, byType.get(ActionType.DELETE).size());
        assertEquals(2, byType.get(ActionType.WARNING).size());
        assertEquals(2, byType.get(ActionType.IGNORE).size());
    }



}