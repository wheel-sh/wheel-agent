package sh.wheel.gitops.agent.config;

import lombok.Data;

import java.util.List;

@Data
public class GroupConfig {
    private String name;
    private List<PoolConfig> pools;
}
