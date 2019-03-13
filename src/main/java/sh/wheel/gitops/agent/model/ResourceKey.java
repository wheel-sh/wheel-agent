package sh.wheel.gitops.agent.model;

import java.util.Objects;

public class ResourceKey {


    public static final ResourceKey PROJECT = projectWithName(null);

    private String kind;
    private String name;
    private String apiVersion;

    public ResourceKey(String name, String kind, String apiVersion) {
        this.kind = kind;
        this.name = name;
        this.apiVersion = apiVersion;
    }

    public ResourceKey() {
    }

    public static ResourceKey projectWithName(String name) {
        return new ResourceKey(name, "Project", "project.openshift.io/v1");
    }


    public String getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceKey that = (ResourceKey) o;
        return Objects.equals(kind, that.kind) &&
                Objects.equals(name, that.name) &&
                Objects.equals(apiVersion, that.apiVersion);
    }

    public boolean equalsKindAndName(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceKey that = (ResourceKey) o;
        return Objects.equals(kind, that.kind) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, name, apiVersion);
    }

    @Override
    public String toString() {
        return "ResourceKey{" +
                "kind='" + kind + '\'' +
                ", name='" + name + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                '}';
    }
}
