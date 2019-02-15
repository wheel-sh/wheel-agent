package sh.wheel.gitops.agent.okd;

import io.fabric8.kubernetes.client.utils.ReplaceValueStream;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Disabled
@SuppressWarnings("all")
public class OpenShiftClientSnippetsIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(OpenShiftClientSnippetsIntegrationTest.class);

    @Test
    void name() {
        OpenShiftClient osClient = new DefaultOpenShiftClient();

        Map<String, String> params = new HashMap<>();
        params.put("REPLICA_COUNT", "2");
        params.put("IMAGE_NAME", "bitnami/nginx");
        params.put("IMAGE_VERSION", "1.14-ol-7");
        osClient.templates().load(ReplaceValueStream.replaceValues(this.getClass().getResourceAsStream("/samples/testrepo1/apps/example-app/template/app.v1.yaml"), params)).processLocally(params).getItems().forEach(p -> osClient.resource(p).inNamespace("test2").createOrReplace());
        System.out.println();
    }
}
