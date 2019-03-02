package sh.wheel.gitops.agent.model;

import java.util.Objects;

public class IgnoredAttributeIdentifier  {
    private final String attributeName;
    private final String value;

    public IgnoredAttributeIdentifier(String attributeName, String value) {
        this.attributeName = attributeName;
        this.value = value;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IgnoredAttributeIdentifier that = (IgnoredAttributeIdentifier) o;
        return Objects.equals(attributeName, that.attributeName) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeName, value);
    }
}
