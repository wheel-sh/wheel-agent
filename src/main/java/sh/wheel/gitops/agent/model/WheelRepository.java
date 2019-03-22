package sh.wheel.gitops.agent.model;

import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Value
public class WheelRepository {

    private final Map<String, App> apps;
    private final Map<String, Group> groups;
    private final BaseConfig baseConfig;

    public static WheelRepository newInstance(List<App> apps, List<Group> groups, BaseConfig baseConfig) {
        Map<String, App> appsMap = apps.stream().collect(Collectors.toMap(App::getName, Function.identity()));
        Map<String, Group> groupsMap = groups.stream().collect(Collectors.toMap(g -> g.getGroupConfig().getName(), Function.identity()));
        return new WheelRepository(appsMap, groupsMap, baseConfig);
    }
}
