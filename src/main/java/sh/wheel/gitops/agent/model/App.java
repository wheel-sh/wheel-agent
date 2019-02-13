package sh.wheel.gitops.agent.model;

import sh.wheel.gitops.agent.config.AppConfig;
import sh.wheel.gitops.agent.config.NamespaceConfig;
import sh.wheel.gitops.agent.config.TemplateConfig;

import java.util.List;

public class App {

    private final AppConfig appConfig;
    private final List<BuildConfig> buildConfigPaths;
    private final List<NamespaceConfig> namespaceConfigPaths;
    private final List<TemplateConfig> templateConfigsPath;

    public App(AppConfig appConfig, List<BuildConfig> buildConfigPaths, List<NamespaceConfig> namespaceConfigPaths, List<TemplateConfig> templateConfigsPath) {
        this.appConfig = appConfig;
        this.buildConfigPaths = buildConfigPaths;
        this.namespaceConfigPaths = namespaceConfigPaths;
        this.templateConfigsPath = templateConfigsPath;
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public List<BuildConfig> getBuildConfigPaths() {
        return buildConfigPaths;
    }

    public List<NamespaceConfig> getNamespaceConfigPaths() {
        return namespaceConfigPaths;
    }

    public List<TemplateConfig> getTemplateConfigsPath() {
        return templateConfigsPath;
    }
}
