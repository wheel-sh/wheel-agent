package sh.wheel.gitops.agent.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.model.ApiResource;
import sh.wheel.gitops.agent.model.ProjectState;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenShiftServiceIntegrationTest {


    private OpenShiftService openShiftService;

    @BeforeEach
    void setUp() {
        openShiftService = new OpenShiftService();
        openShiftService.init();
    }

    @Test
    void getManagableResources() {
        List<ApiResource> manageableResources = openShiftService.getManageableResources();
        assertTrue(manageableResources.size() > 40);
    }

    @Test
    void getProjectStatesFromCluster() {
        List<ProjectState> projectStatesFromCluster = openShiftService.getProjectStatesFromCluster();

        assertEquals(1, projectStatesFromCluster.size());
    }
}