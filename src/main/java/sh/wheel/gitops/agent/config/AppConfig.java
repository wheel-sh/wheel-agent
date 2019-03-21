package sh.wheel.gitops.agent.config;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class AppConfig {
    private String group;
    private Map metadata;
}
