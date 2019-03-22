package sh.wheel.gitops.agent.model;

import lombok.Value;

import java.util.Map;

@Value
public class ProjectState {
    private final Resource project;
    private final Map<ResourceKey, Resource> resourcesByKey;

    public String getName() {
        return project.getName();
    }
}
