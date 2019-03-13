package sh.wheel.gitops.agent.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.model.ProjectState;
import sh.wheel.gitops.agent.model.Resource;
import sh.wheel.gitops.agent.model.ResourceKey;
import sh.wheel.gitops.agent.testutil.OpenShiftCliMockUtil;
import sh.wheel.gitops.agent.testutil.Samples;
import sh.wheel.gitops.agent.util.OpenShiftCli;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
class OpenShiftServiceTest {

    private OpenShiftService openShiftService;

    @BeforeEach
    void setUp() {
        openShiftService = null;
    }

    @Test
    void getAllNamespacedResources() {
        ProjectState projectStateFromCluster = openShiftService.getProjectStateFromCluster("example-app-test");
        Map<ResourceKey, Resource> allNamespacedResources = projectStateFromCluster.getResourcesByKey();

        assertTrue(allNamespacedResources.size() > 0);
    }

    @Test
    void process() {
        Map<String, String> params = new HashMap<>();
        params.put("REPLICA_COUNT", "2");
        params.put("IMAGE_NAME", "bitnami/nginx");
        params.put("IMAGE_VERSION", "1.14-ol-7");

        Map<ResourceKey, Resource> process = openShiftService.process(Samples.TEMPLATE1.toPath(), params);

        assertTrue(process.size() > 2);
    }

    @Test
    void clusterState() {
        List<ProjectState> projectStatesFromCluster = openShiftService.getProjectStatesFromCluster();

        assertNotNull(projectStatesFromCluster);
        assertEquals(1, projectStatesFromCluster.size());
    }
}