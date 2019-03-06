package sh.wheel.gitops.agent.api;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class GitHookRestController {

    @RequestMapping("/hook")
    public String doGet() {
        return "Hello from Spring Boot";
    }
}
