package sh.wheel.gitops.agent.model;

import lombok.Value;

@Value
public class IgnoredAttributeIdentifier {
    private final String attributeName;
    private final String value;
}
