package sh.wheel.gitops.agent.model;

import lombok.Value;

import java.util.List;

@Value
public class ResourceDifference {
    private final ResourcePresence presence;
    private final Resource processed;
    private final Resource cluster;
    private final List<AttributeDifference> attributeDifferences;
}
