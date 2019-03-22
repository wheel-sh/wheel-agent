package sh.wheel.gitops.agent.model;

import lombok.Value;

@Value
public class ResourceKey {
    public static final ResourceKey PROJECT = projectWithName(null);
    private final String name;
    private final String kind;
    private final String apiVersion;

    public static ResourceKey projectWithName(String name) {
        return new ResourceKey(name, "Project", "project.openshift.io/v1");
    }
}
