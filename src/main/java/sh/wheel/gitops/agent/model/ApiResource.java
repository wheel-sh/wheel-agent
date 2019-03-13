package sh.wheel.gitops.agent.model;

import java.util.List;
import java.util.Objects;

public class ApiResource {

    private final String name;
    private final String kind;
    private final String groupName;
    private final String apiVersion;
    private final boolean coreApi;
    private final boolean subresource;
    private final boolean namespaced;
    private final List<String> verbs;

    public ApiResource(String name, String kind, String groupName, String apiVersion, boolean coreApi, boolean subresource, boolean namespaced, List<String> verbs) {
        this.name = name;
        this.kind = kind;
        this.groupName = groupName;
        this.apiVersion = apiVersion;
        this.coreApi = coreApi;
        this.subresource = subresource;
        this.namespaced = namespaced;
        this.verbs = verbs;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public boolean isCoreApi() {
        return coreApi;
    }

    public boolean isSubresource() {
        return subresource;
    }

    public boolean isNamespaced() {
        return namespaced;
    }

    public String getApiEndpoint(String namespace) {
        String api = coreApi ? "/api" : "/apis";
        String version = "/" + apiVersion;
        String namespaced = isNamespaced() ? "/namespaces/" + namespace : "";
        String name = "/" + getName();
        return api + version + namespaced + name;
    }

    public List<String> getVerbs() {
        return verbs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiResource that = (ApiResource) o;
        return coreApi == that.coreApi &&
                subresource == that.subresource &&
                namespaced == that.namespaced &&
                Objects.equals(name, that.name) &&
                Objects.equals(kind, that.kind) &&
                Objects.equals(groupName, that.groupName) &&
                Objects.equals(apiVersion, that.apiVersion) &&
                Objects.equals(verbs, that.verbs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, kind, groupName, apiVersion, coreApi, subresource, namespaced, verbs);
    }

    @Override
    public String toString() {
        return "ApiResource{" +
                "name='" + name + '\'' +
                ", kind='" + kind + '\'' +
                ", groupName='" + groupName + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", coreApi=" + coreApi +
                ", subresource=" + subresource +
                ", namespaced=" + namespaced +
                ", verbs=" + verbs +
                '}';
    }

    public static ApiResourceBuilder newBuilder() {
        return new ApiResourceBuilder();
    }


    public static final class ApiResourceBuilder {
        private String name;
        private String kind;
        private String groupName;
        private String apiVersion;
        private boolean coreApi;
        private boolean subresource;
        private boolean namespaced;
        private List<String> verbs;

        private ApiResourceBuilder() {
        }

        public static ApiResourceBuilder anApiResource() {
            return new ApiResourceBuilder();
        }

        public ApiResourceBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ApiResourceBuilder kind(String kind) {
            this.kind = kind;
            return this;
        }

        public ApiResourceBuilder groupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public ApiResourceBuilder apiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        public ApiResourceBuilder coreApi(boolean coreApi) {
            this.coreApi = coreApi;
            return this;
        }

        public ApiResourceBuilder subresource(boolean subresource) {
            this.subresource = subresource;
            return this;
        }

        public ApiResourceBuilder namespaced(boolean namespaced) {
            this.namespaced = namespaced;
            return this;
        }

        public ApiResourceBuilder verbs(List<String> verbs) {
            this.verbs = verbs;
            return this;
        }

        public ApiResource build() {
            return new ApiResource(name, kind, groupName, apiVersion, coreApi, subresource, namespaced, verbs);
        }
    }
}
