package sh.wheel.gitops.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.DiffFlags;
import com.flipkart.zjsonpatch.JsonDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.model.*;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProjectDifferenceService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public List<ResourceDifference> evaluateDifference(ProjectState processed, ProjectState cluster) {
        List<ResourceDifference> resourceDifferences = new ArrayList<>();
        Map<String, List<Resource>> processedResourcesByKind = processed.getResourcesByKind();
        Map<String, List<Resource>> clusterResourcesByKind = cluster.getResourcesByKind();
        Set<String> availableKinds = Stream.concat(processedResourcesByKind.keySet().stream(), clusterResourcesByKind.keySet().stream()).collect(Collectors.toSet());
        for (String kind : availableKinds) {
            List<Resource> processedResources = processedResourcesByKind.get(kind);
            List<Resource> clusterResources = clusterResourcesByKind.get(kind);
            Map<String, Resource> processedResourceByName = groupByName(processedResources);
            Map<String, Resource> clusterResourceByName = groupByName(clusterResources);
            Set<String> availableResourceNames = Stream.concat(processedResourceByName.keySet().stream(), clusterResourceByName.keySet().stream()).collect(Collectors.toSet());
            for (String name : availableResourceNames) {
                Resource processedResource = processedResourceByName.get(name);
                Resource clusterResource = clusterResourceByName.get(name);
                ResourceKey resourceKey = new ResourceKey(kind, name);
                if (processedResource == null) {
                    resourceDifferences.add(new ResourceDifference(DifferenceType.PROJECT_ONLY, null, clusterResource, null, resourceKey));
                } else if (clusterResource == null) {
                    resourceDifferences.add(new ResourceDifference(DifferenceType.PROCESSED_ONLY, processedResource, null, null, resourceKey));
                } else {
                    List<AttributeDifference> attributeDifferences = evaluateAttributeDifference(processedResource, clusterResource);
                    resourceDifferences.add(new ResourceDifference(DifferenceType.DIFFER, processedResource, clusterResource, attributeDifferences, resourceKey));
                }
            }
        }
        return resourceDifferences;
    }

    private Map<String, Resource> groupByName(List<Resource> processedResources) {
        return processedResources != null ? processedResources.stream().collect(Collectors.toMap(Resource::getName, Function.identity())) : new HashMap<>();
    }


    private List<AttributeDifference> evaluateAttributeDifference(Resource proccessed, Resource cluster) {
        JsonNode processedJsonNode = proccessed.getJsonNode();
        JsonNode clusterJsonNode = cluster.getJsonNode();
        if (processedJsonNode == null && clusterJsonNode == null) {
            throw new IllegalStateException("Cannot evaluate difference between two null resources");
        }
        JsonNode diffs = JsonDiff.asJson(processedJsonNode, clusterJsonNode, DiffFlags.dontNormalizeOpIntoMoveAndCopy());
        List<AttributeDifference> attributeDifferences = new ArrayList<>();
        for (JsonNode diff : diffs) {
            String path = diff.get("path").textValue();
            Operation op = Operation.byName(diff.get("op").textValue());
            JsonNode jsonNodeValue = diff.get("value");
            String value = jsonNodeValue.textValue() != null ? jsonNodeValue.textValue() : jsonNodeValue.toString();
            attributeDifferences.add(new AttributeDifference(proccessed.getName(), proccessed.getKind(), path, value, op));
        }
        return attributeDifferences;
    }

    private boolean isAddedByDefault(JsonNode diff) {
        return "add".equals(diff.get("op").textValue());
    }

    private boolean isRemoveEmptyNode(JsonNode diff) {
        JsonNode value = diff.get("value");
        return "remove".equals(diff.get("op").textValue()) && value.isObject() && value.size() == 0;
    }


    private static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
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
