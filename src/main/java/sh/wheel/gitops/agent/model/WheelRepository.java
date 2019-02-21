package sh.wheel.gitops.agent.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WheelRepository {

    private Map<String, App> apps;
    private Map<String, Group> groups;

    public WheelRepository() {
    }

    public WheelRepository(Map<String, App> apps, Map<String, Group> groups) {
        this.apps = apps;
        this.groups = groups;
    }

    public static WheelRepository newInstance(List<App> apps, List<Group> groups) {
        Map<String, App> appsMap = apps.stream().collect(Collectors.toMap(a -> a.getAppConfig().getName(), Function.identity()));
        Map<String, Group> groupsMap = groups.stream().collect(Collectors.toMap(Group::getName, Function.identity()));
        return new WheelRepository(appsMap, groupsMap);
    }

    public Map<String, App> getApps() {
        return apps;
    }

    public Map<String, Group> getGroups() {
        return groups;
    }
}
