package sh.wheel.gitops.agent.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class AttributeDifference {
    private String resourceKind;
    private String resourceName;
    private String attributeName;
    private String attributeValue;
    private Operation operation;
    private JsonNode diff;
}
