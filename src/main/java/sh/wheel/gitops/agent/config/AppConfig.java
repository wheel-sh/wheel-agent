package sh.wheel.gitops.agent.config;

import java.util.Map;

public class AppConfig {
    private String group;
    private Map metadata;

    public Map getMetadata() {
        return metadata;
    }

    public void setMetadata(Map metadata) {
        this.metadata = metadata;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
