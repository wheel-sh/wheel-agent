package sh.wheel.gitops.agent.api;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sh.wheel.gitops.agent.service.AgentService;


@RestController
public class GitHookRestController {

    private AgentService agentService;

    @Autowired
    public GitHookRestController(AgentService agentService) {
        this.agentService = agentService;
    }

    @RequestMapping("/hook")
    public String doGet() {
        return "Hello from Spring Boot";
    }

    @RequestMapping("/sync")
    public String sync() {
        this.callAgentService();
        return "Synchronization started...";
    }

    @Async
    void callAgentService() {
        agentService.synchronize();
    }
}
