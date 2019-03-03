package sh.wheel.gitops.agent.model;

import java.nio.file.Path;

public class BaseConfig {
    private Path projectTemplate;

    public BaseConfig() {
    }

    public BaseConfig(Path projectTemplate) {
        this.projectTemplate = projectTemplate;
    }

    public Path getProjectTemplate() {
        return projectTemplate;
    }

    @Override
    public String toString() {
        return "BaseConfig{" +
                "projectTemplate=" + projectTemplate +
                '}';
    }
}
