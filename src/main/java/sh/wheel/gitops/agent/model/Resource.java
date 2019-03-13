package sh.wheel.gitops.agent.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

public class Resource {

    private final ResourceKey resourceKey;
    private String uid;
    private final JsonNode jsonNode;

    public Resource(ResourceKey resourceKey, String uid, JsonNode jsonNode) {
        this.resourceKey = resourceKey;
        this.uid = uid;
        this.jsonNode = jsonNode;
    }

    public String getKind() {
        return resourceKey.getKind();
    }

    public String getName() {
        return resourceKey.getName();
    }

    public JsonNode getJsonNode() {
        return jsonNode;
    }

    public String getApiVersion() {
        return resourceKey.getApiVersion();
    }

    public String getUid() {
        return uid;
    }

    public ResourceKey getResourceKey() {
        return resourceKey;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "resourceKey=" + resourceKey +
                ", uid='" + uid + '\'' +
                ", jsonNode=" + jsonNode +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(resourceKey, resource.resourceKey) &&
                Objects.equals(uid, resource.uid) &&
                Objects.equals(jsonNode, resource.jsonNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceKey, uid, jsonNode);
    }
}
