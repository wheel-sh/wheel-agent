package sh.wheel.gitops.agent.model;

import java.util.List;

public class ResourceAction {
    private final ActionType type;
    private final Resource resource;
    private List<AttributeDifference> attributeDifferences;

    public ResourceAction(ActionType type, Resource resource, List<AttributeDifference> attributeDifferences) {
        this.type = type;
        this.resource = resource;
        this.attributeDifferences = attributeDifferences;
    }

    public ActionType getType() {
        return type;
    }

    public Resource getResource() {
        return resource;
    }

    public List<AttributeDifference> getAttributeDifferences() {
        return attributeDifferences;
    }

    @Override
    public String toString() {
        return "ResourceAction{" +
                "type=" + type +
                ", resource=" + resource +
                ", attributeDifferences=" + attributeDifferences +
                '}';
    }
}
