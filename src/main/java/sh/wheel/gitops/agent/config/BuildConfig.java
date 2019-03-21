package sh.wheel.gitops.agent.config;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class BuildConfig {
    private String name;
    private String gitUrl;
    private String jenkinsfilePath;
    private List<ParameterConfig> env;
}