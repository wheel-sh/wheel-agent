package sh.wheel.gitops.agent.model;

import java.util.List;
import java.util.Objects;

public class ApiResource {
    private final String name;
    private boolean subresource;
    private final String kind;
    private final String apiGroup;
    private final String apiVersion;
    private final boolean namespaced;
    private final List<String> verbs;

    public ApiResource(String name, boolean subresource, String kind, String apiGroup, String apiVersion, boolean namespaced, List<String> verbs) {
        this.name = name;
        this.subresource = subresource;
        this.kind = kind;
        this.apiGroup = apiGroup;
        this.apiVersion = apiVersion;
        this.namespaced = namespaced;
        this.verbs = verbs;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public String getApiGroup() {
        return apiGroup;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public boolean isNamespaced() {
        return namespaced;
    }

    public List<String> getVerbs() {
        return verbs;
    }

    public boolean isSubresource() {
        return subresource;
    }

    @Override
    public String toString() {
        return "ApiResource{" +
                "name='" + name + '\'' +
                ", subresource=" + subresource +
                ", kind='" + kind + '\'' +
                ", apiGroup='" + apiGroup + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", namespaced=" + namespaced +
                ", verbs=" + verbs +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiResource that = (ApiResource) o;
        return subresource == that.subresource &&
                namespaced == that.namespaced &&
                Objects.equals(name, that.name) &&
                Objects.equals(kind, that.kind) &&
                Objects.equals(apiGroup, that.apiGroup) &&
                Objects.equals(apiVersion, that.apiVersion) &&
                Objects.equals(verbs, that.verbs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, subresource, kind, apiGroup, apiVersion, namespaced, verbs);
    }
}
