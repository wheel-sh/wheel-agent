package sh.wheel.gitops.agent.model;

import java.util.List;
import java.util.Map;

public class Repository {

    private Map<String, App> apps;
    private Map<String, List<Group>> groups;

    public Repository() {
    }

    public Repository(Map<String, App> apps, Map<String, List<Group>> groups) {
        this.apps = apps;
        this.groups = groups;
    }

    public Map<String, App> getApps() {
        return apps;
    }

    public Map<String, List<Group>> getGroups() {
        return groups;
    }
}
