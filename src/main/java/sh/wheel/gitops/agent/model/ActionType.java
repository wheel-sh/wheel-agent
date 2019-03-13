package sh.wheel.gitops.agent.model;

public enum ActionType {
    CREATE,
    DELETE,
    PATCH,
    IGNORE_CLUSTER_ATTR,
    IGNORE_OWNED_RESOURCE
}
