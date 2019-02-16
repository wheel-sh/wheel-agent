package sh.wheel.gitops.agent.okd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.utils.ReplaceValueStream;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.model.ProjectResources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProjectResourceLoaderIntegrationTest {

    @Test
    void getAll() {

        DefaultOpenShiftClient client = new DefaultOpenShiftClient();
        ProjectResourceLoader projectResourceLoader = new ProjectResourceLoader();

        ProjectResources test2 = projectResourceLoader.getAll("test2", client);

        assertNotNull(test2);

    }

    @Test
    void loadAll() {
        DefaultOpenShiftClient client = new DefaultOpenShiftClient();
        ProjectResourceLoader projectResourceLoader = new ProjectResourceLoader();

        Map<Class, List<HasMetadata>> test2 = projectResourceLoader.loadAll("test2", client);

        assertNotNull(test2);
    }

    @Test
    void templateCompare() {
        OpenShiftClient client = new DefaultOpenShiftClient();

        Map<String, String> params = new HashMap<>();
        params.put("REPLICA_COUNT", "2");
        params.put("IMAGE_NAME", "bitnami/nginx");
        params.put("IMAGE_VERSION", "1.14-ol-7");
        List<HasMetadata> items = client.templates().load(ReplaceValueStream.replaceValues(this.getClass().getResourceAsStream("/samples/testrepo1/apps/example-app/template/app.v1.yaml"), params)).processLocally(params).getItems();

        ProjectResourceLoader projectResourceLoader = new ProjectResourceLoader();

        Map<Class, List<HasMetadata>> test2 = projectResourceLoader.loadAll("test2", client);
        ObjectMapper mapper = new ObjectMapper();
        for (HasMetadata item : items) {
            if(test2.containsKey(item.getClass())) {
                JsonNode jsonNode = mapper.valueToTree(item);
                HasMetadata hasMetadata = test2.get(item.getClass()).get(0);
                JsonNode jsonNode1 = mapper.valueToTree(hasMetadata);
                System.out.println();
            }
        }
    }
}