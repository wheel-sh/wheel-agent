package sh.wheel.gitops.agent.model;

public class ResourceDifference {

    private String resourceName;
    private String resourceKind;
    private String attributeName;
    private String attributeValue;
    private Operation operation;

    public ResourceDifference(String resourceName, String resourceKind, String attributeName, String attributeValue, Operation operation) {
        this.resourceName = resourceName;
        this.resourceKind = resourceKind;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.operation = operation;
    }


    public String getResourceName() {
        return resourceName;
    }

    public String getResourceKind() {
        return resourceKind;
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
}
