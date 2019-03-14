package sh.wheel.gitops.agent.component;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sh.wheel.gitops.agent.util.OpenShiftRestClient;

@Configuration
public class OpenShiftRestClientFactory {

    @Bean
    public OpenShiftRestClient createOpenShiftRestClient() {
        return OpenShiftRestClient.create();
    }
}
