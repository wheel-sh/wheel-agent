package sh.wheel.gitops.agent.okd;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.dsl.Listable;
import io.fabric8.openshift.client.OpenShiftClient;
import sh.wheel.gitops.agent.model.ProjectResources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectResourceLoader {


    public Map<Class, List<HasMetadata>> loadAll(String namespace, OpenShiftClient client) {
        List<KubernetesResourceList> resources = new ArrayList<>();
        resources.add(client.routes().inNamespace(namespace).list());
        resources.add(client.services().inNamespace(namespace).list());
        resources.add(client.deploymentConfigs().inNamespace(namespace).list());

        Map<Class, List<HasMetadata>> collect = resources.stream().filter(rl -> rl.getItems().size() > 0).collect(
                Collectors.toMap(
                        o -> o.getItems().get(0).getClass(),
                        o -> o.getItems())
        );
        return collect;
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

