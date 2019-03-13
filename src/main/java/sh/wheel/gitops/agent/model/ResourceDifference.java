package sh.wheel.gitops.agent.model;

import java.util.List;

public class ResourceDifference {

    private final ResourcePresence presence;
    private final Resource resource;
    private final Resource cluster;
    private final List<AttributeDifference> attributeDifferences;

    public ResourceDifference(ResourcePresence presence, Resource processed, Resource cluster, List<AttributeDifference> attributeDifferences) {
        this.presence = presence;
        this.resource = processed;
        this.cluster = cluster;
        this.attributeDifferences = attributeDifferences;
    }

    public ResourcePresence getPresence() {
        return presence;
    }

    public Resource getResource() {
        return resource;
    }

    public Resource getCluster() {
        return cluster;
    }

    public List<AttributeDifference> getAttributeDifferences() {
        return attributeDifferences;
    }

    @Override
    public String toString() {
        return "ResourceDifference{" +
                "presence=" + presence +
                ", resource=" + resource +
                ", cluster=" + cluster +
                ", attributeDifferences=" + attributeDifferences +
                '}';
    }
}
