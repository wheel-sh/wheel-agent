package sh.wheel.gitops.agent.config;

import lombok.Data;

import java.util.List;

@Data
public class MembersConfig {
    List<MemberConfig> members;
}
