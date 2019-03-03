package sh.wheel.gitops.agent.service;

import io.fabric8.kubernetes.api.builder.BaseFluent;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;

@Service
public class ProjectResourceService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final OpenShiftClient client;
    private MixedOperation<? extends HasMetadata, KubernetesResourceList<HasMetadata>, ? extends BaseFluent, Resource<? extends HasMetadata, ? extends BaseFluent>>[] resourceOperations;

    @Autowired
    public ProjectResourceService(OpenShiftClient client) {
        this.client = client;
    }

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void setClientResources() {
        resourceOperations = new MixedOperation[]{
                client.routes(),
                client.services(),
                client.buildConfigs(),
                client.imageStreams(),
                client.imageStreamTags(),
                client.roles(),
                client.roleBindings(),
                client.routes(),
                client.templates(),
                client.limitRanges(),
                client.pods(),
                client.persistentVolumeClaims(),
                client.replicationControllers(),
                client.resourceQuotas(),
                client.serviceAccounts(),
                client.secrets(),
                client.configMaps(),
                client.persistentVolumeClaims(),
        };
    }

//    public ProjectState getProjectStateFromCluster(String namespace) {
//        List<HasMetadata> namespaceResources = Arrays.stream(resourceOperations).parallel()
//                .map(ro -> ro.inNamespace(namespace).list().getItems())
//                .flatMap(Collection::stream)
//                .collect(Collectors.toList());
//        return new ProjectState(namespace, namespaceResources);
//
//    }

    //    public ProjectResources getAll(String namespace, OpenShiftClient client) {
//        return ProjectResources.newBuilder()
//                .buildConfigList(client.buildConfigs().inNamespace(namespace).list())
//                .imageStreamList(client.imageStreams().inNamespace(namespace).list())
//                .imageStreamTagList(client.imageStreamTags().inNamespace(namespace).list())
//                .roleList(client.roles().inNamespace(namespace).list())
//                .roleBindingList(client.roleBindings().inNamespace(namespace).list())
//                .routeList(client.routes().inNamespace(namespace).list())
//                .templateList(client.templates().inNamespace(namespace).list())
//                .limitRangeList(client.limitRanges().inNamespace(namespace).list())
//                .podList(client.pods().inNamespace(namespace).list())
//                .persistentVolumeClaimList(client.persistentVolumeClaims().inNamespace(namespace).list())
//                .replicationControllerList(client.replicationControllers().inNamespace(namespace).list())
//                .resourceQuotaList(client.resourceQuotas().inNamespace(namespace).list())
//                .serviceList(client.services().inNamespace(namespace).list())
//                .serviceAccountList(client.serviceAccounts().inNamespace(namespace).list())
//                .secretList(client.secrets().inNamespace(namespace).list())
//                .configMapList(client.configMaps().inNamespace(namespace).list())
//                .build();
//    }

//        namespaceResources.addAll(Collections.unmodifiableList(client.routes().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.services().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.buildConfigs().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.imageStreams().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.imageStreamTags().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.roles().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.roleBindings().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.routes().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.templates().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.limitRanges().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.pods().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.persistentVolumeClaims().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.replicationControllers().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.resourceQuotas().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.serviceAccounts().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.secrets().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.configMaps().inNamespace(namespace).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.persistentVolumeClaims().inNamespace(namespace).list().getItems()));
}
