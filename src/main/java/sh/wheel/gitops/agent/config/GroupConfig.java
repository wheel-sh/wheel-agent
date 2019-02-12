package sh.wheel.gitops.agent.config;

import java.util.List;

public class GroupConfig {

    private String name;
    private List<PoolConfig> pools;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PoolConfig> getPools() {
        return pools;
    }

    public void setPools(List<PoolConfig> pools) {
        this.pools = pools;
    }
}
