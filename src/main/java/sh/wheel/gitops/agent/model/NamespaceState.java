package sh.wheel.gitops.agent.model;

import java.util.List;
import java.util.Map;

public class NamespaceState {
    private String name;
    private Map<String, List<Resource>> resourcesByKind;

    public NamespaceState(String name, Map<String, List<Resource>> resourcesByKind) {
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
