package sh.wheel.gitops.agent.config;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ResourceConfig {
    private String cpu;
    private String memory;
}
