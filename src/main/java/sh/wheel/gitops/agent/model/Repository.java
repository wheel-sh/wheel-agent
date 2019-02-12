package sh.wheel.gitops.agent.model;

import java.util.List;

public class Repository {
    List<Group> groups;
    List<App> apps;

public List<Group> getGroups() {
        return groups;
    }

    public List<App> getApps() {
        return apps;
    }
}
