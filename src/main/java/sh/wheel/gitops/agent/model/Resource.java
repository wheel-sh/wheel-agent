package sh.wheel.gitops.agent.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(kind, resource.kind) &&
                Objects.equals(name, resource.name) &&
                Objects.equals(jsonNode, resource.jsonNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, name, jsonNode);
    }

    @Override
    public String toString() {
        return "Resource{" +
                "kind='" + kind + '\'' +
                ", name='" + name + '\'' +
                ", jsonNode=" + jsonNode +
                '}';
    }
}
