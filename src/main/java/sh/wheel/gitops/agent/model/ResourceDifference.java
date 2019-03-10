package sh.wheel.gitops.agent.model;

import java.util.List;
import java.util.Objects;

public class ResourceDifference {

    private final DifferenceType type;
    private final Resource processed;
    private final Resource project;
    private final List<AttributeDifference> attributeDifferences;
    private ResourceKey resourceKey;

    public ResourceDifference(DifferenceType differ, Resource processed, Resource project, List<AttributeDifference> attributeDifferences, ResourceKey resourceKey) {
        type = differ;
        this.processed = processed;
        this.project = project;
        this.attributeDifferences = attributeDifferences;
        this.resourceKey = resourceKey;
    }

    public DifferenceType getType() {
        return type;
    }

    public Resource getProcessed() {
        return processed;
    }

    public Resource getProject() {
        return project;
    }

    public List<AttributeDifference> getAttributeDifferences() {
        return attributeDifferences;
    }

    public ResourceKey getResourceKey() {
        return resourceKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceDifference that = (ResourceDifference) o;
        return type == that.type &&
                Objects.equals(processed, that.processed) &&
                Objects.equals(project, that.project) &&
                Objects.equals(attributeDifferences, that.attributeDifferences) &&
                Objects.equals(resourceKey, that.resourceKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, processed, project, attributeDifferences, resourceKey);
    }
}
