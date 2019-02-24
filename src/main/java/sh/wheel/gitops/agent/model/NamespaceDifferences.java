package sh.wheel.gitops.agent.model;

import java.util.List;
import java.util.Map;

public class NamespaceDifferences {

    private String namespace;
    private Map<ResourceKey, List<ResourceDifference>> resourceDifferences;

    public NamespaceDifferences() {
    }

    public NamespaceDifferences(String namespace, Map<ResourceKey, List<ResourceDifference>> resourceDifferences) {
        this.namespace = namespace;
        this.resourceDifferences = resourceDifferences;
    }

    public String getNamespace() {
        return namespace;
    }

    public Map<ResourceKey, List<ResourceDifference>> getResourceDifferences() {
        return resourceDifferences;
    }
}
