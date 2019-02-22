package sh.wheel.gitops.agent.rest;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HelloWorldEndpoint {

    @RequestMapping("/hello")
    public String doGet() {
        return "Hello from Spring Boot";
    }
}
