package sh.wheel.gitops.agent.model;

import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;

public class NamespaceState {
    private String name;
    List<HasMetadata> resources;

    public NamespaceState(String name, List<HasMetadata> resources) {
        this.name = name;
        this.resources = resources;
    }

    public String getName() {
        return name;
    }

    public List<HasMetadata> getResources() {
        return resources;
    }
}
