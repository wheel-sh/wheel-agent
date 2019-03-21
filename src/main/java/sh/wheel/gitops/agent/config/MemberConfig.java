package sh.wheel.gitops.agent.config;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MemberConfig {
    private String name;
    private String userId;
    private String role;
}

