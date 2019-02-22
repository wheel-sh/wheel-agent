package sh.wheel.gitops.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.DiffFlags;
import com.flipkart.zjsonpatch.JsonDiff;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.model.NamespaceDifferences;
import sh.wheel.gitops.agent.model.NamespaceState;
import sh.wheel.gitops.agent.model.Operation;
import sh.wheel.gitops.agent.model.ResourceDifference;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NamespaceDiffService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public NamespaceDifferences evaluateDifference(NamespaceState actual, NamespaceState expected) {
        Map<String, List<HasMetadata>> expectedByKind = expected.getResourcesByKind();
        Map<String, List<HasMetadata>> actualByKind = actual.getResourcesByKind();
        Set<String> availableStates = new HashSet<>();
        availableStates.addAll(expectedByKind.keySet());
        availableStates.addAll(actualByKind.keySet());
        for (String availableState : availableStates) {
            Map<String, HasMetadata> expectedResourceByName = expectedByKind.get(availableState).stream().collect(Collectors.toMap(r -> r.getMetadata().getName(), Function.identity()));
            Map<String, HasMetadata> actualResourceByName = actualByKind.get(availableState).stream().collect(Collectors.toMap(r -> r.getMetadata().getName(), Function.identity()));
            Set<String> availableResourceNames = new HashSet<>();
            availableResourceNames.addAll(expectedResourceByName.keySet());
            availableResourceNames.addAll(actualResourceByName.keySet());
            for (String availableResourceName : availableResourceNames) {
                HasMetadata expectedResource = expectedResourceByName.get(availableResourceName);
                HasMetadata actualResource = actualResourceByName.get(availableResourceName);
                evaluateDiff(expectedResource, actualResource);
            }

        }
        return null;
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
}
