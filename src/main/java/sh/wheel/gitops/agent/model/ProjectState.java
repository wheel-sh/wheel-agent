package sh.wheel.gitops.agent.model;

import java.util.List;
import java.util.Map;

public class ProjectState {
    private String name;
    private Map<String, List<Resource>> resourcesByKind;

    public ProjectState(String name, Map<String, List<Resource>> resourcesByKind) {
        this.name = name;
        this.resourcesByKind = resourcesByKind;
    }

    public String getName() {
        return name;
    }

    public Map<String, List<Resource>> getResourcesByKind() {
        return resourcesByKind;
    }
}
