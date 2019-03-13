package sh.wheel.gitops.agent.component;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sh.wheel.gitops.agent.util.OpenShiftTemplateUtil;

@Configuration
public class OpenShiftTemplateUtilFactory {

    @Bean
    public OpenShiftTemplateUtil createOpenShiftTemplateUtil() {
        return OpenShiftTemplateUtil.create();
    }
}
