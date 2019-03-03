package sh.wheel.gitops.agent.model;

import sh.wheel.gitops.agent.config.AppConfig;
import sh.wheel.gitops.agent.config.BuildConfig;
import sh.wheel.gitops.agent.config.ProjectConfig;

import java.nio.file.Path;
import java.util.List;

public class App {


    private final AppConfig appConfig;
    private final List<BuildConfig> buildConfigs;
    private final List<ProjectConfig> projectConfigs;
    private final Path appDir;

    public App(AppConfig appConfig, List<BuildConfig> buildConfigs, List<ProjectConfig> projectConfigs, Path appDir) {
        this.appConfig = appConfig;
        this.buildConfigs = buildConfigs;
        this.projectConfigs = projectConfigs;
        this.appDir = appDir;
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public List<BuildConfig> getBuildConfigs() {
        return buildConfigs;
    }

    public List<ProjectConfig> getProjectConfigs() {
        return projectConfigs;
    }

    public Path getAppDir() {
        return appDir;
    }
}
