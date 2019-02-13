package sh.wheel.gitops.agent.model;

import sh.wheel.gitops.agent.config.AppConfig;
import sh.wheel.gitops.agent.config.BuildConfig;
import sh.wheel.gitops.agent.config.NamespaceConfig;

import java.nio.file.Path;
import java.util.List;

public class App {


    private final AppConfig appConfig;
    private final List<BuildConfig> buildConfigs;
    private final List<NamespaceConfig> namespaceConfigs;
    private final Path appDir;

    public App(AppConfig appConfig, List<BuildConfig> buildConfigs, List<NamespaceConfig> namespaceConfigs, Path appDir) {
        this.appConfig = appConfig;
        this.buildConfigs = buildConfigs;
        this.namespaceConfigs = namespaceConfigs;
        this.appDir = appDir;
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public List<BuildConfig> getBuildConfigs() {
        return buildConfigs;
    }

    public List<NamespaceConfig> getNamespaceConfigs() {
        return namespaceConfigs;
    }

    public Path getAppDir() {
        return appDir;
    }
}
