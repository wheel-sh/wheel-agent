package sh.wheel.gitops.agent.api;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sh.wheel.gitops.agent.service.AgentService;
import sh.wheel.gitops.agent.service.OpenShiftService;
import sh.wheel.gitops.agent.service.StateService;

import java.lang.invoke.MethodHandles;


@RestController
public class WheelRestController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private AgentService agentService;
    private StateService stateService;
    private OpenShiftService openShiftService;

    @Autowired
    public WheelRestController(AgentService agentService, StateService stateService, OpenShiftService openShiftService) {
        this.agentService = agentService;
        this.stateService = stateService;
        this.openShiftService = openShiftService;
    }

    @RequestMapping("/hook")
    public String doGet() {
        return "Hello from Spring Boot";
    }

    @RequestMapping("/git-hook")
    public String gitHOok() {
        callStateService();
        this.callAgentService();
        return "Sync successful";
    }

    @RequestMapping("/sync")
    public String sync() {
        this.callStateService();
        this.callAgentService();
        return "Synchronization started...";
    }

    @RequestMapping("/refresh")
    public String refresh() {
        this.callOpenShiftService();
        return "Reload started...";
    }

    @Async
    void callAgentService() {
        agentService.synchronize();
    }

    void callStateService() {
        try {
            stateService.init();
        } catch (Exception e) {
            LOG.warn("Error occured while calling StateService", e);
        }
    }

    void callOpenShiftService() {
        try {
            openShiftService.init();
        } catch (Exception e) {
            LOG.warn("Error occured while calling OpenShiftService", e);
        }
    }
}
