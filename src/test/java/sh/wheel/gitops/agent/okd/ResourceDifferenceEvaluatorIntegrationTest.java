package sh.wheel.gitops.agent.okd;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;
import sh.wheel.gitops.agent.util.ReplaceValueStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ResourceDifferenceEvaluatorIntegrationTest {

    @Test
    void compare() {
        OpenShiftClient client = new DefaultOpenShiftClient();
        Map<String, String> params = new HashMap<>();
        params.put("REPLICA_COUNT", "2");
        params.put("IMAGE_NAME", "bitnami/nginx");
        params.put("IMAGE_VERSION", "1.14-ol-7");
        List<HasMetadata> templateResources = client.templates().load(ReplaceValueStream.replaceValues(this.getClass().getResourceAsStream("/samples/testrepo1/apps/example-app/template/app.v1.yaml"), params)).processLocally(params).getItems();
        ProjectResourceLoader projectResourceLoader = new ProjectResourceLoader();
        Map<String, List<HasMetadata>> projectResources = projectResourceLoader.loadAll("test2", client);

        HasMetadata templateService = templateResources.stream().filter(tr -> tr.getKind().equals("Service")).findFirst().get();
        HasMetadata projectService = projectResources.get("Service").get(0);

        HasMetadata templateDeploymentConfig = templateResources.stream().filter(tr -> tr.getKind().equals("DeploymentConfig")).findFirst().get();
        HasMetadata projectDeploymentConfig = projectResources.get("DeploymentConfig").get(0);

        HasMetadata templateRoute = templateResources.stream().filter(tr -> tr.getKind().equals("Route")).findFirst().get();
        HasMetadata projectRoute = projectResources.get("Route").get(0);


        boolean serviceChanged = new ResourceDifferenceEvaluator().evaluateDiff(templateService, projectService);
        boolean dcChanged = new ResourceDifferenceEvaluator().evaluateDiff(templateDeploymentConfig, projectDeploymentConfig);
        boolean routeChanged = new ResourceDifferenceEvaluator().evaluateDiff(templateRoute, projectRoute);

        Assert.assertFalse(serviceChanged);
        Assert.assertFalse(dcChanged);
        Assert.assertFalse(routeChanged);

    }
}