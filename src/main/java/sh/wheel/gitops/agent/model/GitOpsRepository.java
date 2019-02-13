package sh.wheel.gitops.agent.model;

import java.util.List;

public class GitOpsRepository {
    List<Group> groups;
    List<App> apps;

    public GitOpsRepository(List<App> apps, List<Group> groups) {
        this.groups = groups;
        this.apps = apps;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public List<App> getApps() {
        return apps;
    }
}
