package sh.wheel.gitops.agent.model;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.openshift.api.model.*;

public class ProjectResources {
    BuildConfigList buildConfigList;
    ImageStreamList imageStreamList;
    ImageStreamTagList imageStreamTagList;
    RoleList roleList;
    RoleBindingList roleBindingList;
    RouteList routeList;
    TemplateList templateList;
    LimitRangeList limitRangeList;
    PodList podList;
    PersistentVolumeClaimList persistentVolumeClaimList;
    ReplicationControllerList replicationControllerList;
    ResourceQuotaList resourceQuotaList;
    ServiceList serviceList;
    ServiceAccountList serviceAccountList;
    SecretList secretList;
    ConfigMapList configMapList;

    private ProjectResources() {
    }

    public BuildConfigList getBuildConfigList() {
        return buildConfigList;
    }

    public ImageStreamList getImageStreamList() {
        return imageStreamList;
    }

    public ImageStreamTagList getImageStreamTagList() {
        return imageStreamTagList;
    }

    public RoleList getRoleList() {
        return roleList;
    }

    public RoleBindingList getRoleBindingList() {
        return roleBindingList;
    }

    public RouteList getRouteList() {
        return routeList;
    }

    public TemplateList getTemplateList() {
        return templateList;
    }

    public LimitRangeList getLimitRangeList() {
        return limitRangeList;
    }

    public PodList getPodList() {
        return podList;
    }

    public PersistentVolumeClaimList getPersistentVolumeClaimList() {
        return persistentVolumeClaimList;
    }

    public ReplicationControllerList getReplicationControllerList() {
        return replicationControllerList;
    }

    public ResourceQuotaList getResourceQuotaList() {
        return resourceQuotaList;
    }

    public ServiceList getServiceList() {
        return serviceList;
    }

    public ServiceAccountList getServiceAccountList() {
        return serviceAccountList;
    }

    public SecretList getSecretList() {
        return secretList;
    }

    public ConfigMapList getConfigMapList() {
        return configMapList;
    }

    public static ProjectResourcesBuilder newBuilder() {
        return new ProjectResourcesBuilder();
    }

    public static final class ProjectResourcesBuilder {
        BuildConfigList buildConfigList;
        ImageStreamList imageStreamList;
        ImageStreamTagList imageStreamTagList;
        RoleList roleList;
        RoleBindingList roleBindingList;
        RouteList routeList;
        TemplateList templateList;
        LimitRangeList limitRangeList;
        PodList podList;
        PersistentVolumeClaimList persistentVolumeClaimList;
        ReplicationControllerList replicationControllerList;
        ResourceQuotaList resourceQuotaList;
        ServiceList serviceList;
        ServiceAccountList serviceAccountList;
        SecretList secretList;
        ConfigMapList configMapList;

        private ProjectResourcesBuilder() {
        }

        public static ProjectResourcesBuilder aProjectResources() {
            return new ProjectResourcesBuilder();
        }

        public ProjectResourcesBuilder buildConfigList(BuildConfigList buildConfigList) {
            this.buildConfigList = buildConfigList;
            return this;
        }

        public ProjectResourcesBuilder imageStreamList(ImageStreamList imageStreamList) {
            this.imageStreamList = imageStreamList;
            return this;
        }

        public ProjectResourcesBuilder imageStreamTagList(ImageStreamTagList imageStreamTagList) {
            this.imageStreamTagList = imageStreamTagList;
            return this;
        }

        public ProjectResourcesBuilder roleList(RoleList roleList) {
            this.roleList = roleList;
            return this;
        }

        public ProjectResourcesBuilder roleBindingList(RoleBindingList roleBindingList) {
            this.roleBindingList = roleBindingList;
            return this;
        }

        public ProjectResourcesBuilder routeList(RouteList routeList) {
            this.routeList = routeList;
            return this;
        }

        public ProjectResourcesBuilder templateList(TemplateList templateList) {
            this.templateList = templateList;
            return this;
        }

        public ProjectResourcesBuilder limitRangeList(LimitRangeList list) {
            this.limitRangeList = list;
            return this;
        }

        public ProjectResourcesBuilder podList(PodList podList) {
            this.podList = podList;
            return this;
        }

        public ProjectResourcesBuilder persistentVolumeClaimList(PersistentVolumeClaimList persistentVolumeClaimList) {
            this.persistentVolumeClaimList = persistentVolumeClaimList;
            return this;
        }

        public ProjectResourcesBuilder replicationControllerList(ReplicationControllerList replicationControllerList) {
            this.replicationControllerList = replicationControllerList;
            return this;
        }

        public ProjectResourcesBuilder resourceQuotaList(ResourceQuotaList resourceQuotaList) {
            this.resourceQuotaList = resourceQuotaList;
            return this;
        }

        public ProjectResourcesBuilder serviceList(ServiceList serviceList) {
            this.serviceList = serviceList;
            return this;
        }

        public ProjectResourcesBuilder serviceAccountList(ServiceAccountList serviceAccountList) {
            this.serviceAccountList = serviceAccountList;
            return this;
        }

        public ProjectResourcesBuilder secretList(SecretList secretList) {
            this.secretList = secretList;
            return this;
        }

        public ProjectResourcesBuilder configMapList(ConfigMapList configMapList) {
            this.configMapList = configMapList;
            return this;
        }

        public ProjectResources build() {
            ProjectResources projectResources = new ProjectResources();
            projectResources.configMapList = this.configMapList;
            projectResources.roleBindingList = this.roleBindingList;
            projectResources.buildConfigList = this.buildConfigList;
            projectResources.imageStreamList = this.imageStreamList;
            projectResources.secretList = this.secretList;
            projectResources.serviceList = this.serviceList;
            projectResources.replicationControllerList = this.replicationControllerList;
            projectResources.limitRangeList = this.limitRangeList;
            projectResources.persistentVolumeClaimList = this.persistentVolumeClaimList;
            projectResources.resourceQuotaList = this.resourceQuotaList;
            projectResources.podList = this.podList;
            projectResources.routeList = this.routeList;
            projectResources.imageStreamTagList = this.imageStreamTagList;
            projectResources.roleList = this.roleList;
            projectResources.templateList = this.templateList;
            projectResources.serviceAccountList = this.serviceAccountList;
            return projectResources;
        }
    }
}
