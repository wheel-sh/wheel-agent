package sh.wheel.gitops.agent.config;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class ParameterConfig {
    private String name;
    private String value;
}
