package sh.wheel.gitops.agent.config;

public class AppConfig {
    private String name;

    public AppConfig(String name) {
        this.name = name;
    }

    public AppConfig() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
