package sh.wheel.gitops.agent.okd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.DiffFlags;
import com.flipkart.zjsonpatch.JsonDiff;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.ArrayList;
import java.util.List;

public class ResourceDifferenceEvaluator {

    public List<JsonNode> evaluateDiff(HasMetadata templateResource, HasMetadata actualResource) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode templateNodeTree = mapper.valueToTree(templateResource);
        JsonNode actualResourceNodeTree = mapper.valueToTree(actualResource);
        JsonNode diffs = JsonDiff.asJson(templateNodeTree, actualResourceNodeTree, DiffFlags.dontNormalizeOpIntoMoveAndCopy());
        List<JsonNode> relevantDiffs = new ArrayList<>();
        for (JsonNode diff : diffs) {
            String path = diff.get("path").textValue();
            if (!isAddedByDefault(diff) && !isRemoveEmptyNode(diff)) {
                relevantDiffs.add(diff);
            }
        }
        return relevantDiffs;
    }

    private boolean isAddedByDefault(JsonNode diff) {
        return "add".equals(diff.get("op").textValue());
    }

    private boolean isRemoveEmptyNode(JsonNode diff) {
        JsonNode value = diff.get("value");
        return "remove".equals(diff.get("op").textValue()) && value.isObject() && value.size() == 0;
    }
}