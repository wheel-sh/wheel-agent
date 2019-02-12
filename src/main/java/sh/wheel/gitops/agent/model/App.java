package sh.wheel.gitops.agent.model;

import java.util.List;

public class App {

    private String name;
    private Group group;
    private List<Namespace> spaces;
    private List<Template> templates;
    private List<BuildConfig> buildConfigs;

    public App(String name, Group group, List<Namespace> spaces, List<Template> templates, List<BuildConfig> buildConfigs) {
        this.name = name;
        this.group = group;
        this.spaces = spaces;
        this.templates = templates;
        this.buildConfigs = buildConfigs;
    }

    public String getName() {
        return name;
    }

    public Group getGroup() {
        return group;
    }

    public List<Namespace> getSpaces() {
        return spaces;
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public List<BuildConfig> getBuildConfigs() {
        return buildConfigs;
    }
}
