package sh.wheel.gitops.agent.model;

import sh.wheel.gitops.agent.config.GroupConfig;
import sh.wheel.gitops.agent.config.MembersConfig;

public class Group {

    private final GroupConfig groupConfig;
    private final MembersConfig membersConfig;

    public Group(GroupConfig groupConfig, MembersConfig membersConfig) {
        this.groupConfig = groupConfig;
        this.membersConfig = membersConfig;
    }

    public GroupConfig getGroupConfig() {
        return groupConfig;
    }

    public MembersConfig getMembersConfig() {
        return membersConfig;
    }
}
