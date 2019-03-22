package sh.wheel.gitops.agent.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ApiResource {

    private final String name;
    private final String kind;
    private final String groupName;
    private final String apiVersion;
    private final boolean coreApi;
    private final boolean subresource;
    private final boolean namespaced;
    private final List<String> verbs;

    public String getApiEndpoint(String namespace) {
        String api = coreApi ? "/api" : "/apis";
        String version = "/" + apiVersion;
        String namespacePath = namespaced ? "/namespaces/" + namespace : "";
        String resourceName = "/" + name;
        return api + version + namespacePath + resourceName;
    }
}
