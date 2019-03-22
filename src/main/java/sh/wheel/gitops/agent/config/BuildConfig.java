package sh.wheel.gitops.agent.config;

import lombok.Data;

import java.util.List;

@Data
public class BuildConfig {
    private String name;
    private String gitUrl;
    private String jenkinsfilePath;
    private List<ParameterConfig> env;
}