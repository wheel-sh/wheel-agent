package sh.wheel.gitops.agent.config;

public class PoolConfig {

    private String name;
    private ResourceConfig requests;
    private ResourceConfig limits;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourceConfig getRequests() {
        return requests;
    }

    public void setRequests(ResourceConfig requests) {
        this.requests = requests;
    }

    public ResourceConfig getLimits() {
        return limits;
    }

    public void setLimits(ResourceConfig limits) {
        this.limits = limits;
    }
}
