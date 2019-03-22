package sh.wheel.gitops.agent.api;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sh.wheel.gitops.agent.service.AgentService;
import sh.wheel.gitops.agent.service.OpenShiftService;
import sh.wheel.gitops.agent.service.StateService;

import java.lang.invoke.MethodHandles;


@RestController
public class WheelRestController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AgentService agentService;
    private final StateService stateService;
    private final OpenShiftService openShiftService;

    @Autowired
    public WheelRestController(AgentService agentService, StateService stateService, OpenShiftService openShiftService) {
        this.agentService = agentService;
        this.stateService = stateService;
        this.openShiftService = openShiftService;
    }

    @RequestMapping("/git-hook")
    public String gitHOok() {
        try {
            stateService.init();
            agentService.synchronize();
            return "Sync successful";
        } catch (Exception e) {
            LOG.warn("Error occured while calling StateService", e);
            return "Sync failed";
        }
    }

    @RequestMapping("/sync")
    public String sync() {
        try {
            stateService.init();
            agentService.synchronize();
            return "Synchronization finished successful";
        } catch (Exception e) {
            LOG.warn("Error occured while calling StateService", e);
            return "Error while syncing";
        }
    }

    @RequestMapping("/refresh")
    public String refresh() {
        try {
            openShiftService.init();
            return "Reload completed";
        } catch (Exception e) {
            LOG.warn("Error occured while calling OpenShiftService", e);
            return "Reload failed";
        }
    }

}
