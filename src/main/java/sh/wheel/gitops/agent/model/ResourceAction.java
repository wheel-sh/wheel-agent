package sh.wheel.gitops.agent.model;

import lombok.Value;

import java.util.List;

@Value
public class ResourceAction {
    private final ActionType type;
    private final Resource resource;
    private List<AttributeDifference> attributeDifferences;
}
