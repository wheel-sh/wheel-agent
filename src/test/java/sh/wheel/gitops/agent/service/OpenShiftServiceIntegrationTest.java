package sh.wheel.gitops.agent.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.model.ApiResource;
import sh.wheel.gitops.agent.model.ProjectState;
import sh.wheel.gitops.agent.util.OpenShiftRestClient;
import sh.wheel.gitops.agent.util.OpenShiftTemplateUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenShiftServiceIntegrationTest {


    private OpenShiftService openShiftService;

    @BeforeEach
    void setUp() {
        OpenShiftTemplateUtil templateUtil = OpenShiftTemplateUtil.create();
        OpenShiftRestClient openShiftRestClient = OpenShiftRestClient.create();
        openShiftService = new OpenShiftService(templateUtil, openShiftRestClient);
        openShiftService.init();
    }

    @Test
    void getManagableResources() {
        List<ApiResource> manageableResources = openShiftService.getManageableResources();
        assertTrue(manageableResources.size() > 40);
    }

    @Test
    void getProjectStatesFromCluster() {
        long start = System.currentTimeMillis();
        List<ProjectState> projectStatesFromCluster = openShiftService.getProjectStatesFromCluster();
        System.out.println("Time: " + (System.currentTimeMillis() - start));
        assertEquals(1, projectStatesFromCluster.size());
    }
}