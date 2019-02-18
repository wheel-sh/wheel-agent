package sh.wheel.gitops.agent.okd;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.openshift.client.OpenShiftClient;
import sh.wheel.gitops.agent.model.ProjectResources;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectResourceLoader {


    public Map<String, List<HasMetadata>> loadAll(String namespace, OpenShiftClient client) {
        Map<String, List<HasMetadata>> resources = new HashMap<>();;
        resources.put("Route", Collections.unmodifiableList(client.routes().inNamespace(namespace).list().getItems()));
        resources.put("Service", Collections.unmodifiableList(client.services().inNamespace(namespace).list().getItems()));
        resources.put("DeploymentConfig", Collections.unmodifiableList(client.deploymentConfigs().inNamespace(namespace).list().getItems()));
        return resources;
    }

    public ProjectResources getAll(String namespace, OpenShiftClient client) {
        return ProjectResources.newBuilder()
                .buildConfigList(client.buildConfigs().inNamespace(namespace).list())
                .imageStreamList(client.imageStreams().inNamespace(namespace).list())
                .imageStreamTagList(client.imageStreamTags().inNamespace(namespace).list())
                .roleList(client.roles().inNamespace(namespace).list())
                .roleBindingList(client.roleBindings().inNamespace(namespace).list())
                .routeList(client.routes().inNamespace(namespace).list())
                .templateList(client.templates().inNamespace(namespace).list())
                .limitRangeList(client.limitRanges().inNamespace(namespace).list())
                .podList(client.pods().inNamespace(namespace).list())
                .persistentVolumeClaimList(client.persistentVolumeClaims().inNamespace(namespace).list())
                .replicationControllerList(client.replicationControllers().inNamespace(namespace).list())
                .resourceQuotaList(client.resourceQuotas().inNamespace(namespace).list())
                .serviceList(client.services().inNamespace(namespace).list())
                .serviceAccountList(client.serviceAccounts().inNamespace(namespace).list())
                .secretList(client.secrets().inNamespace(namespace).list())
                .configMapList(client.configMaps().inNamespace(namespace).list())
                .build();
    }
}

