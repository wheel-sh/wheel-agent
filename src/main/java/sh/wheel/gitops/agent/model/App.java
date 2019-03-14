package sh.wheel.gitops.agent.model;

import sh.wheel.gitops.agent.config.AppConfig;
import sh.wheel.gitops.agent.config.BuildConfig;
import sh.wheel.gitops.agent.config.EnvConfig;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class App {
    private String name;
    private final AppConfig appConfig;
    private final List<BuildConfig> buildConfigs;
    private final Map<String, EnvConfig> envConfigs;
    private final Path appDir;

    public App(String name, AppConfig appConfig, List<BuildConfig> buildConfigs, Map<String, EnvConfig> envConfigs, Path appDir) {
        this.name = name;
        this.appConfig = appConfig;
        this.buildConfigs = buildConfigs;
        this.envConfigs = envConfigs;
        this.appDir = appDir;
    }

    public String getName() {
        return name;
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public List<BuildConfig> getBuildConfigs() {
        return buildConfigs;
    }

    public Map<String, EnvConfig> getEnvConfigs() {
        return envConfigs;
    }

    public Path getAppDir() {
        return appDir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        App app = (App) o;
        return Objects.equals(name, app.name) &&
                Objects.equals(appConfig, app.appConfig) &&
                Objects.equals(buildConfigs, app.buildConfigs) &&
                Objects.equals(envConfigs, app.envConfigs) &&
                Objects.equals(appDir, app.appDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, appConfig, buildConfigs, envConfigs, appDir);
    }

    @Override
    public String toString() {
        return "App{" +
                "name='" + name + '\'' +
                ", appConfig=" + appConfig +
                ", buildConfigs=" + buildConfigs +
                ", envConfigs=" + envConfigs +
                ", appDir=" + appDir +
                '}';
    }
}
