package sh.wheel.gitops.agent.config;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class EnvConfig {
    private String pool;
    private ResourceConfig requests;
    private ResourceConfig limits;
    private String templateFile;
    private List<ParameterConfig> parameters;
}