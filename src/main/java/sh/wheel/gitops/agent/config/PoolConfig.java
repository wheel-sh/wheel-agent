package sh.wheel.gitops.agent.config;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class PoolConfig {
    private String name;
    private ResourceConfig requests;
    private ResourceConfig limits;
}
