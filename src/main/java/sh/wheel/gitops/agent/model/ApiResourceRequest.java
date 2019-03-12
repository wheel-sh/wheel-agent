package sh.wheel.gitops.agent.model;

import java.util.Objects;

public class ApiResourceRequest {
    private final String apiEndpoint;
    private final String groupName;

    public ApiResourceRequest(String apiEndpoint, String groupName) {
        this.apiEndpoint = apiEndpoint;
        this.groupName = groupName;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiResourceRequest that = (ApiResourceRequest) o;
        return Objects.equals(apiEndpoint, that.apiEndpoint) &&
                Objects.equals(groupName, that.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiEndpoint, groupName);
    }

    @Override
    public String toString() {
        return "ApiResourceRequest{" +
                "apiEndpoint='" + apiEndpoint + '\'' +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}
