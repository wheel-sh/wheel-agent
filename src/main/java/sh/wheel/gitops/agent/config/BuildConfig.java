package sh.wheel.gitops.agent.config;

import java.util.List;

public class BuildConfig {
    private String name;
    private String gitUrl;
    private String jenkinsfilePath;
    private List<ParameterConfig> env;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getJenkinsfilePath() {
        return jenkinsfilePath;
    }

    public void setJenkinsfilePath(String jenkinsfilePath) {
        this.jenkinsfilePath = jenkinsfilePath;
    }

    public List<ParameterConfig> getEnv() {
        return env;
    }

    public void setEnv(List<ParameterConfig> env) {
        this.env = env;
    }
}