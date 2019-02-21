package sh.wheel.gitops.agent.model;

import io.fabric8.kubernetes.api.model.HasMetadata;

public class NamespaceResource {

    private String name;
    private String type;
    private HasMetadata resource;

    public NamespaceResource(String name, String type, HasMetadata resource) {
        this.name = name;
        this.type = type;
        this.resource = resource;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public HasMetadata getResource() {
        return resource;
    }
}
