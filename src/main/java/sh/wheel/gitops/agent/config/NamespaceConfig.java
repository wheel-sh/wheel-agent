package sh.wheel.gitops.agent.config;

import java.util.List;

public class NamespaceConfig {

    private String name;
    private String pool;
    private ResourceConfig requests;
    private ResourceConfig limits;
    private TemplateConfig template;
    private List<ParameterConfig> parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public TemplateConfig getTemplate() {
        return template;
    }

    public void setTemplate(TemplateConfig template) {
        this.template = template;
    }

    public List<ParameterConfig> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterConfig> parameters) {
        this.parameters = parameters;
    }
}