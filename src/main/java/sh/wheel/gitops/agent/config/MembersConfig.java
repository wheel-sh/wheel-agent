package sh.wheel.gitops.agent.config;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MembersConfig {
    List<MemberConfig> members;
}
