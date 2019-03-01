package sh.wheel.gitops.agent.model;

import java.util.List;

public class ResourceDifference {

    private final DifferenceType type;
    private final Resource processed;
    private final Resource project;
    private final List<AttributeDifference> attributeDifferences;

    public ResourceDifference(DifferenceType differ, Resource processed, Resource project, List<AttributeDifference> attributeDifferences) {
        type = differ;
        this.processed = processed;
        this.project = project;
        this.attributeDifferences = attributeDifferences;
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
}
