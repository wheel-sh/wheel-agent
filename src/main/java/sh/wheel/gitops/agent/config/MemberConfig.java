package sh.wheel.gitops.agent.config;

import lombok.Data;

@Data
public class MemberConfig {
    private String name;
    private String userId;
    private String role;
}

