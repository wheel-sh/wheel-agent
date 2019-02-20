package sh.wheel.gitops.agent;

import sh.wheel.gitops.agent.model.NamespaceState;

import java.util.Map;

public class StateController {

    private Map<String, NamespaceState> clusterState;
    private Map<String, NamespaceState> expectedState;

    public StateController() {
    }

    public Map<String, NamespaceState> getClusterState() {
        return clusterState;
    }

    public Map<String, NamespaceState> getExpectedState() {
        return expectedState;
    }
}
