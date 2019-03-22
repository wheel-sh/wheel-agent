package sh.wheel.gitops.agent.config;

import lombok.Data;

@Data
public class ResourceConfig {
    private String cpu;
    private String memory;
}
