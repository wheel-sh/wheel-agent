package sh.wheel.gitops.agent.model;

import lombok.Value;

import java.nio.file.Path;

@Value
public class BaseConfig {
    private final Path projectTemplate;
}
