package sh.wheel.gitops.agent.config;

import lombok.Data;

import java.util.Map;

@Data
public class AppConfig {
    private String group;
    private Map metadata;
}
