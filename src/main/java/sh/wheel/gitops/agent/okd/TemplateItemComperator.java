package sh.wheel.gitops.agent.okd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import io.fabric8.kubernetes.api.model.HasMetadata;

public class TemplateItemComperator {

    public boolean compare(HasMetadata templateItem, HasMetadata actualResource) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode templateNodeTree = mapper.valueToTree(templateItem);
        JsonNode actualResourceNodeTree = mapper.valueToTree(actualResource);
        return false;
    }

}
