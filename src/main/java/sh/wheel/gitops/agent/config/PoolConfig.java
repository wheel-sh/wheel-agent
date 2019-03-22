package sh.wheel.gitops.agent.config;

import lombok.Data;

@Data
public class PoolConfig {
    private String name;
    private ResourceConfig requests;
    private ResourceConfig limits;
}
