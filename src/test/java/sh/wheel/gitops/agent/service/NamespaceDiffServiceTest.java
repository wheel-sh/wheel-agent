package sh.wheel.gitops.agent.service;

import static org.junit.jupiter.api.Assertions.*;

class NamespaceDiffServiceTest {

    //    @Test
//    void compare() {
//        OpenShiftClient client = new DefaultOpenShiftClient();
//        Map<String, String> params = new HashMap<>();
//        params.put("REPLICA_COUNT", "2");
//        params.put("IMAGE_NAME", "bitnami/nginx");
//        params.put("IMAGE_VERSION", "1.14-ol-7");
//        List<HasMetadata> templateResources = client.templates().load(ReplaceValueStream.replaceValues(this.getClass().getResourceAsStream("/samples/testrepo1/apps/example-app/template/app.v1.yaml"), params)).processLocally(params).getItems();
//        ProjectResourceLoader projectResourceLoader = new ProjectResourceLoader();
//        Map<String, List<HasMetadata>> projectResources = projectResourceLoader.getNamespaceState("test2", client);
//
//        HasMetadata templateService = templateResources.stream().filter(tr -> tr.getKind().equals("Service")).findFirst().get();
//        HasMetadata projectService = projectResources.get("Service").get(0);
//
//        HasMetadata templateDeploymentConfig = templateResources.stream().filter(tr -> tr.getKind().equals("DeploymentConfig")).findFirst().get();
//        HasMetadata projectDeploymentConfig = projectResources.get("DeploymentConfig").get(0);
//
//        HasMetadata templateRoute = templateResources.stream().filter(tr -> tr.getKind().equals("Route")).findFirst().get();
//        HasMetadata projectRoute = projectResources.get("Route").get(0);
//
//
//        boolean serviceChanged = new ResourceDifferenceEvaluator().evaluateDiff(templateService, projectService).size() > 0;
//        boolean dcChanged = new ResourceDifferenceEvaluator().evaluateDiff(templateDeploymentConfig, projectDeploymentConfig).size() > 0;
//        boolean routeChanged = new ResourceDifferenceEvaluator().evaluateDiff(templateRoute, projectRoute).size() > 0;
//
//        Assert.assertFalse(serviceChanged);
//        Assert.assertFalse(dcChanged);
//        Assert.assertFalse(routeChanged);
//
//    }

}