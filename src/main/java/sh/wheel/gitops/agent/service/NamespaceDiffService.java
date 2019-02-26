package sh.wheel.gitops.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.DiffFlags;
import com.flipkart.zjsonpatch.JsonDiff;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.model.*;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NamespaceDiffService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public NamespaceDifferences evaluateDifference(NamespaceState actual, NamespaceState expected) {
        Map<String, List<HasMetadata>> expectedByKind = expected.getResources().stream().collect(Collectors.groupingBy(HasMetadata::getKind));
        Map<String, List<HasMetadata>> actualByKind = actual.getResources().stream().collect(Collectors.groupingBy(HasMetadata::getKind));
        Set<String> availableKinds = new HashSet<>();
        availableKinds.addAll(expectedByKind.keySet());
        availableKinds.addAll(actualByKind.keySet());
        Map<ResourceKey, List<ResourceDifference>> differences = new HashMap<>();
        for (String kind : availableKinds) {
            Map<String, HasMetadata> expectedResourceByName = expectedByKind.get(kind).stream().collect(Collectors.toMap(r -> r.getMetadata().getName(), Function.identity()));
            Map<String, HasMetadata> actualResourceByName = actualByKind.get(kind).stream().collect(Collectors.toMap(r -> r.getMetadata().getName(), Function.identity()));
            Set<String> availableResourceNames = new HashSet<>();
            availableResourceNames.addAll(expectedResourceByName.keySet());
            availableResourceNames.addAll(actualResourceByName.keySet());
            for (String name : availableResourceNames) {
                HasMetadata expectedResource = expectedResourceByName.get(name);
                HasMetadata actualResource = actualResourceByName.get(name);
                List<ResourceDifference> resourceDifferences = evaluateDiff(expectedResource, actualResource);
                differences.put(new ResourceKey(kind, name), resourceDifferences);
            }
        }
        return new NamespaceDifferences(null, differences);
    }


    public List<ResourceDifference> evaluateDiff(HasMetadata expectedResource, HasMetadata actualResource) {
        if (expectedResource == null && actualResource == null) {
            throw new IllegalStateException("Cannot evaluate difference between two null resources");
        }
        HasMetadata referenceResource = expectedResource != null ? expectedResource : actualResource;
        String resourceName = referenceResource.getMetadata().getName();
        String resourceKind = referenceResource.getKind();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode expectedNodeTree = mapper.valueToTree(expectedResource != null ? expectedResource : new Object());
        JsonNode actualResourceNodeTree = mapper.valueToTree(actualResource != null ? actualResource : new Object());
        JsonNode diffs = JsonDiff.asJson(expectedNodeTree, actualResourceNodeTree, DiffFlags.dontNormalizeOpIntoMoveAndCopy());
        List<ResourceDifference> resourceDifferences = new ArrayList<>();
        for (JsonNode diff : diffs) {
            String path = diff.get("path").textValue();
            Operation op = Operation.valueOf(diff.get("op").textValue());
            String value = diff.get("value").textValue();
            resourceDifferences.add(new ResourceDifference(resourceName, resourceKind, path, value, op));
        }
        return resourceDifferences;
    }

    private boolean isAddedByDefault(JsonNode diff) {
        return "add".equals(diff.get("op").textValue());
    }

    private boolean isRemoveEmptyNode(JsonNode diff) {
        JsonNode value = diff.get("value");
        return "remove".equals(diff.get("op").textValue()) && value.isObject() && value.size() == 0;
    }


    public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
        updateNode.fieldNames().forEachRemaining(fieldName -> {
            JsonNode jsonNode = mainNode.get(fieldName);
            // if field exists and is an embedded object
            if (jsonNode != null && jsonNode.isObject()) {
                merge(jsonNode, updateNode.get(fieldName));
            } else {
                if (mainNode instanceof ObjectNode) {
                    // Overwrite field
                    JsonNode value = updateNode.get(fieldName);
                    ((ObjectNode) mainNode).replace(fieldName, value);
                }
            }
        });
        return mainNode;
    }

}
