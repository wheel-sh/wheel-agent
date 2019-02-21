package sh.wheel.gitops.agent.model;

import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;
import java.util.Map;

public class NamespaceState {
    Map<String, List<HasMetadata>> resourcesByKind;
    private String name;

    public NamespaceState(String name, Map<String, List<HasMetadata>> resourcesByKind) {
        this.name = name;
        this.resourcesByKind = resourcesByKind;
    }

    public String getName() {
        return name;
    }

    public Map<String, List<HasMetadata>> getResourcesByKind() {
        return resourcesByKind;
    }
}
