package sh.wheel.gitops.agent.model;

import com.fasterxml.jackson.databind.JsonNode;

public class Resource {

    private final String kind;
    private final String name;
    private final JsonNode jsonNode;

    public Resource(String kind, String name, JsonNode jsonNode) {
        this.kind = kind;
        this.name = name;
        this.jsonNode = jsonNode;
    }

    public String getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }

    public JsonNode getJsonNode() {
        return jsonNode;
    }
}
