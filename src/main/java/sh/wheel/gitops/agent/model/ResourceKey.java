package sh.wheel.gitops.agent.model;

import java.util.Objects;

public class ResourceKey {
    private String kind;
    private String name;

    public ResourceKey(String kind, String name) {
        this.kind = kind;
        this.name = name;
    }

    public ResourceKey() {
    }


    public String getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceKey that = (ResourceKey) o;
        return Objects.equals(kind, that.kind) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, name);
    }
}
