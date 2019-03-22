package sh.wheel.gitops.agent.model;

import lombok.Value;

@Value
public class IgnoredResourceIdentifier {
    private final String kind;
    private final String type;
    private final String name;
}
