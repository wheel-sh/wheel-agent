package sh.wheel.gitops.agent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sh.wheel.gitops.agent.util.OpenShiftRestClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class OpenShiftRestClientFactory {

    public static final String TOKEN_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";

    @Value("${OPENSHIFT_API_SERVER:https://kubernetes.default.svc:443}")
    String apiServerUrl;

    @Value("${OPENSHIFT_API_TOKEN:}")
    String apiToken;

    @Bean
    public OpenShiftRestClient createOpenShiftRestClient() {
        if (apiToken == null || apiToken.isEmpty()) {
            Path tokenPath = Paths.get(TOKEN_PATH);
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                apiToken = Files.lines(tokenPath).findFirst().orElse(null);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return OpenShiftRestClient.create(apiServerUrl, apiToken);
    }
}
