package sh.wheel.gitops.agent.config;

import lombok.Data;

import java.util.List;

@Data
public class EnvConfig {
    private String pool;
    private ResourceConfig requests;
    private ResourceConfig limits;
    private String templateFile;
    private List<ParameterConfig> parameters;
}