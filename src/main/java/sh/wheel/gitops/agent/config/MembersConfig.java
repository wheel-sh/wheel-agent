package sh.wheel.gitops.agent.config;

import java.util.List;

public class MembersConfig {

    List<MemberConfig> members;

    public List<MemberConfig> getMembers() {
        return members;
    }

    public void setMembers(List<MemberConfig> members) {
        this.members = members;
    }
}
