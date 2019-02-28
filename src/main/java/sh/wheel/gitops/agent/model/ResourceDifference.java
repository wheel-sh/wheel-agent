package sh.wheel.gitops.agent.model;

import java.util.List;

public class ResourceDifference {

    private List<JsonNodeDifference> jsonNodeDifferences;

    public ResourceDifference(List<JsonNodeDifference> jsonNodeDifferences) {
        this.jsonNodeDifferences = jsonNodeDifferences;
    }
}
