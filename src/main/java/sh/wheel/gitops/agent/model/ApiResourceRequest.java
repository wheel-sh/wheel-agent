package sh.wheel.gitops.agent.model;

import lombok.Value;

@Value
public class ApiResourceRequest {
    private final String apiEndpoint;
    private final String groupName;
}
