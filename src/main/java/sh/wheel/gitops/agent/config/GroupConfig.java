package sh.wheel.gitops.agent.config;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class GroupConfig {
    private String name;
    private List<PoolConfig> pools;
}
