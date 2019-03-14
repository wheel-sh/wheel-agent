package sh.wheel.gitops.agent.config;

import java.util.List;

public class EnvConfig {

    private String pool;
    private ResourceConfig requests;
    private ResourceConfig limits;
    private String templateFile;
    private List<ParameterConfig> parameters;

    public String getPool() {
        return pool;
    }

    public void setPool(String pool) {
        this.pool = pool;
    }

    public ResourceConfig getRequests() {
        return requests;
    }

    public void setRequests(ResourceConfig requests) {
        this.requests = requests;
    }

    public ResourceConfig getLimits() {
        return limits;
    }

    public void setLimits(ResourceConfig limits) {
        this.limits = limits;
    }

    public String getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }

    public List<ParameterConfig> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterConfig> parameters) {
        this.parameters = parameters;
    }
}