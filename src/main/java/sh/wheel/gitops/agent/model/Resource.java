package sh.wheel.gitops.agent.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class Resource {
    private final ResourceKey resourceKey;
    private String uid;
    private final JsonNode jsonNode;

    public String getKind() {
        return resourceKey.getKind();
    }

    public String getName() {
        return resourceKey.getName();
    }

    public String getApiVersion() {
        return resourceKey.getApiVersion();
    }
}
