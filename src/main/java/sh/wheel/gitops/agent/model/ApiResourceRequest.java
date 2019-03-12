package sh.wheel.gitops.agent.model;

import java.util.Objects;

public class ApiResourceRequest {
    private final String apiEndpoint;
    private final String apiGroup;

    public ApiResourceRequest(String apiEndpoint, String apiGroup) {
        this.apiEndpoint = apiEndpoint;
        this.apiGroup = apiGroup;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public String getApiGroup() {
        return apiGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiResourceRequest that = (ApiResourceRequest) o;
        return Objects.equals(apiEndpoint, that.apiEndpoint) &&
                Objects.equals(apiGroup, that.apiGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiEndpoint, apiGroup);
    }

    @Override
    public String toString() {
        return "ApiResourceRequest{" +
                "apiEndpoint='" + apiEndpoint + '\'' +
                ", apiGroup='" + apiGroup + '\'' +
                '}';
    }
}
