package sh.wheel.gitops.agent.model;

import java.util.Objects;

public class AttributeDifference {

    private String resourceKind;
    private String resourceName;
    private String attributeName;
    private String attributeValue;
    private Operation operation;

    public AttributeDifference(String resourceKind, String resourceName, String attributeName, String attributeValue, Operation operation) {
        this.resourceKind = resourceKind;
        this.resourceName = resourceName;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.operation = operation;
    }


    public String getResourceKind() {
        return resourceKind;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public Operation getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return "AttributeDifference{" +
                "resourceKind='" + resourceKind + '\'' +
                ", resourceName='" + resourceName + '\'' +
                ", attributeName='" + attributeName + '\'' +
                ", attributeValue='" + attributeValue + '\'' +
                ", operation=" + operation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeDifference that = (AttributeDifference) o;
        return Objects.equals(resourceKind, that.resourceKind) &&
                Objects.equals(resourceName, that.resourceName) &&
                Objects.equals(attributeName, that.attributeName) &&
                Objects.equals(attributeValue, that.attributeValue) &&
                operation == that.operation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceKind, resourceName, attributeName, attributeValue, operation);
    }
}
