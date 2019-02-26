package sh.wheel.gitops.agent.service;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.model.NamespaceState;

import java.lang.invoke.MethodHandles;
import java.util.*;

@Service
public class ProjectResourceService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final OpenShiftClient client;

    @Autowired
    public ProjectResourceService(OpenShiftClient client) {
        this.client = client;
    }

    public NamespaceState getNamespaceState(String namespace) {
        List<HasMetadata> namespaceResources = new ArrayList<>();
        namespaceResources.addAll(Collections.unmodifiableList(client.routes().inNamespace(namespace).list().getItems()));
        namespaceResources.addAll(Collections.unmodifiableList(client.services().inNamespace(namespace).list().getItems()));
        namespaceResources.addAll(Collections.unmodifiableList(client.deploymentConfigs().inNamespace(namespace).list().getItems()));
        return new NamespaceState(namespace, namespaceResources);
    }

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


}
