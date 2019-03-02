package sh.wheel.gitops.agent.model;

import java.util.Objects;

public class IgnoredResourceIdentifier {
    private final String kind;
    private final String type;
    private String name;

    public IgnoredResourceIdentifier(String kind, String type, String name) {
        this.kind = kind;
        this.type = type;
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IgnoredResourceIdentifier that = (IgnoredResourceIdentifier) o;
        return Objects.equals(kind, that.kind) &&
                Objects.equals(type, that.type) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, type, name);
    }
}
