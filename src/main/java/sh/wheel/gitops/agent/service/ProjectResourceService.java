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

//    public ProjectState getProjectStateFromCluster(String project) {
//        List<HasMetadata> namespaceResources = Arrays.stream(resourceOperations).parallel()
//                .map(ro -> ro.inNamespace(project).list().getItems())
//                .flatMap(Collection::stream)
//                .collect(Collectors.toList());
//        return new ProjectState(project, namespaceResources);
//
//    }

    //    public ProjectResources getAll(String project, OpenShiftClient client) {
//        return ProjectResources.newBuilder()
//                .buildConfigList(client.buildConfigs().inNamespace(project).list())
//                .imageStreamList(client.imageStreams().inNamespace(project).list())
//                .imageStreamTagList(client.imageStreamTags().inNamespace(project).list())
//                .roleList(client.roles().inNamespace(project).list())
//                .roleBindingList(client.roleBindings().inNamespace(project).list())
//                .routeList(client.routes().inNamespace(project).list())
//                .templateList(client.templates().inNamespace(project).list())
//                .limitRangeList(client.limitRanges().inNamespace(project).list())
//                .podList(client.pods().inNamespace(project).list())
//                .persistentVolumeClaimList(client.persistentVolumeClaims().inNamespace(project).list())
//                .replicationControllerList(client.replicationControllers().inNamespace(project).list())
//                .resourceQuotaList(client.resourceQuotas().inNamespace(project).list())
//                .serviceList(client.services().inNamespace(project).list())
//                .serviceAccountList(client.serviceAccounts().inNamespace(project).list())
//                .secretList(client.secrets().inNamespace(project).list())
//                .configMapList(client.configMaps().inNamespace(project).list())
//                .build();
//    }

//        namespaceResources.addAll(Collections.unmodifiableList(client.routes().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.services().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.buildConfigs().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.imageStreams().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.imageStreamTags().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.roles().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.roleBindings().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.routes().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.templates().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.limitRanges().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.pods().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.persistentVolumeClaims().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.replicationControllers().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.resourceQuotas().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.serviceAccounts().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.secrets().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.configMaps().inNamespace(project).list().getItems()));
//        namespaceResources.addAll(Collections.unmodifiableList(client.persistentVolumeClaims().inNamespace(project).list().getItems()));
}
