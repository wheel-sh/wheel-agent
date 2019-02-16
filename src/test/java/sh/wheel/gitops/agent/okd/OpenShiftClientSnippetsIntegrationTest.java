package sh.wheel.gitops.agent.okd;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.utils.ReplaceValueStream;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
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
        List<HasMetadata> items = osClient.templates().load(ReplaceValueStream.replaceValues(this.getClass().getResourceAsStream("/samples/testrepo1/apps/example-app/template/app.v1.yaml"), params)).processLocally(params).getItems();
        items.forEach(p -> osClient.resource(p).inNamespace("test2").createOrReplace());
        System.out.println();
    }

    /*

                ResourceKind.BUILD,
            ResourceKind.BUILD_CONFIG,
            ResourceKind.DEPLOYMENT_CONFIG,
            ResourceKind.IMAGE_STREAM,
            ResourceKind.IMAGE_STREAM_TAG,
//            ResourceKind.IMAGE_STREAM_IMPORT,
//            ResourceKind.OAUTH_ACCESS_TOKEN,
//            ResourceKind.OAUTH_AUTHORIZE_TOKEN,
//            ResourceKind.OAUTH_CLIENT,
//            ResourceKind.OAUTH_CLIENT_AUTHORIZATION,
//            ResourceKind.POLICY,
//            ResourceKind.POLICY_BINDING,
//            ResourceKind.PROJECT,
//            ResourceKind.PROJECT_REQUEST,
            ResourceKind.ROLE,
            ResourceKind.ROLE_BINDING,
            ResourceKind.ROUTE,
            ResourceKind.TEMPLATE,
//            ResourceKind.USER,
//            ResourceKind.GROUP,
//            ResourceKind.IDENTITY,
            // Kubernetes Kinds
            ResourceKind.EVENT,
            ResourceKind.LIMIT_RANGE,
            ResourceKind.POD,
            ResourceKind.PVC,
//            ResourceKind.PERSISTENT_VOLUME,
            ResourceKind.REPLICATION_CONTROLLER,
            ResourceKind.RESOURCE_QUOTA,
            ResourceKind.SERVICE,
            ResourceKind.SECRET,
            ResourceKind.SERVICE_ACCOUNT,
            ResourceKind.CONFIG_MAP


     */
}
