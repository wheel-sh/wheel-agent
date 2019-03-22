package sh.wheel.gitops.agent.model;

import lombok.Value;
import sh.wheel.gitops.agent.config.GroupConfig;
import sh.wheel.gitops.agent.config.MembersConfig;

@Value
public class Group {
    private final GroupConfig groupConfig;
    private final MembersConfig membersConfig;
}
