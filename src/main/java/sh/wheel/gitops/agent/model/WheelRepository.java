package sh.wheel.gitops.agent.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WheelRepository {

    private Map<String, App> apps;
    private Map<String, Group> groups;
    private BaseConfig baseConfig;

    public WheelRepository(Map<String, App> apps, Map<String, Group> groups, BaseConfig baseConfig) {
        this.apps = apps;
        this.groups = groups;
        this.baseConfig = baseConfig;
    }

    public static WheelRepository newInstance(List<App> apps, List<Group> groups, BaseConfig baseConfig) {
        Map<String, App> appsMap = apps.stream().collect(Collectors.toMap(a -> a.getAppConfig().getName(), Function.identity()));
        Map<String, Group> groupsMap = groups.stream().collect(Collectors.toMap(g -> g.getGroupConfig().getName(), Function.identity()));
        return new WheelRepository(appsMap, groupsMap, baseConfig);
    }

    public Map<String, App> getApps() {
        return apps;
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    public BaseConfig getBaseConfig() {
        return baseConfig;
    }
}
