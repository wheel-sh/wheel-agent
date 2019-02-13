package sh.wheel.gitops.agent.config;

import java.util.List;

public class NamespaceConfig {

    private String name;
    private String pool;
    private ResourceConfig requests;
    private ResourceConfig limits;
    private String templateFile;
    private String template;
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

    public String getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public List<ParameterConfig> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterConfig> parameters) {
        this.parameters = parameters;
    }
}