package sh.wheel.gitops.agent.model;

import lombok.Value;

import java.util.Objects;

@Value
public class ResourceKey {
    public static final ResourceKey PROJECT = projectWithName(null);
    private final String name;
    private final String kind;
    private final String apiVersion;

    public static ResourceKey projectWithName(String name) {
        return new ResourceKey(name, "Project", "project.openshift.io/v1");
    }

    public boolean equalsKindAndName(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceKey that = (ResourceKey) o;
        return Objects.equals(kind, that.kind) &&
                Objects.equals(name, that.name);
    }
}
