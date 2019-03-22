package sh.wheel.gitops.agent.model;

import lombok.Value;
import sh.wheel.gitops.agent.config.AppConfig;
import sh.wheel.gitops.agent.config.BuildConfig;
import sh.wheel.gitops.agent.config.EnvConfig;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Value
public class App {
    private String name;
    private final AppConfig appConfig;
    private final List<BuildConfig> buildConfigs;
    private final Map<String, EnvConfig> envConfigs;
    private final Path appDir;
}
