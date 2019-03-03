package sh.wheel.gitops.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import sh.wheel.gitops.agent.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ResourceDifferenceService {


    private static final List<IgnoredResourceIdentifier> IGNORED_RESOURCES = new ArrayList<>();
    private static final List<IgnoredAttributeIdentifier> IGNORED_ATTRIBUTES = new ArrayList<>();

    static {
        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("Secret", "kubernetes.io/dockercfg", null));
        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("Secret", "kubernetes.io/service-account-token", null));
        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("PodMetrics", null, null));
        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("Endpoints", null, null));
        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("ServiceAccount", null, "default"));
        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("ServiceAccount", null, "builder"));
        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("ServiceAccount", null, "deployer"));
        // TODO: implement better default.. So RoleBindings could be edited
//        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("RoleBinding", null, "system:deployers"));
//        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("RoleBinding", null, "system:image-builders"));
//        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("RoleBinding", null, "system:image-pullers"));

        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/status", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/metadata/creationTimestamp", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/metadata/project", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/metadata/resourceVersion", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/metadata/selfLink", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/metadata/uid", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/spec/clusterIP", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/spec/type", "ClusterIP"));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/roleRef/apiGroup", "rbac.authorization.k8s.io"));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/kind", "ClusterRole"));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/apiVersion", "authorization.openshift.io/v1"));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/apiVersion", "rbac.authorization.k8s.io/v1"));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/apiGroup", "rbac.authorization.k8s.io"));
    }

    public List<ResourceAction> createResourceActions(List<ResourceDifference> resourceDifferences, ProjectState processed, ProjectState project) {
        return resourceDifferences.stream()
                .filter(this::filterIgnoredResources)
                .map(resourceDifference -> createAction(resourceDifference, processed, project))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ResourceAction createAction(ResourceDifference resourceDifference, ProjectState processed, ProjectState project) {
        switch (resourceDifference.getType()) {
            case PROCESSED_ONLY:
                return new ResourceAction(ActionType.CREATE, resourceDifference.getProcessed(), resourceDifference.getAttributeDifferences());
            case PROJECT_ONLY:
                return createActionForProjectOnly(resourceDifference, processed, project);
            case DIFFER:
                return createActionForDiff(resourceDifference);
            default:
                throw new IllegalStateException("Unkown DifferenceType "+resourceDifference.getType());
        }
    }

    private ResourceAction createActionForProjectOnly(ResourceDifference resourceDifference, ProjectState processed, ProjectState project) {
        Resource resource = resourceDifference.getProject();
        JsonNode ownerReference = resource.getJsonNode().get("metadata").get("ownerReferences");
        List<Resource> owners = getOwners(ownerReference, project);
        if(!owners.isEmpty() && areOwnersInProcessed(owners, processed)) {
            return new ResourceAction(ActionType.IGNORE, resource, resourceDifference.getAttributeDifferences());
        } else {
            return new ResourceAction(ActionType.DELETE, resource, resourceDifference.getAttributeDifferences());
        }
    }

    private boolean areOwnersInProcessed(List<Resource> owners, ProjectState processed) {
        for (Resource owner : owners) {
            List<Resource> processedByKind = processed.getResourcesByKind().get(owner.getKind());
            boolean foundOwner = processedByKind.stream().anyMatch(r -> r.getName().equals(owner.getName()));
            if(!foundOwner) {
                return false;
            }
        }
        return true;
    }

    private List<Resource> getOwners(JsonNode ownerReferencerces, ProjectState projectState) {
        List<Resource> owners = new ArrayList<>();
        if(ownerReferencerces == null) {
            return owners;
        }
        for (JsonNode ownerReferencerce : ownerReferencerces) {
            String kind = ownerReferencerce.get("kind").textValue();
            String name = ownerReferencerce.get("name").textValue();
            List<Resource> resources = projectState.getResourcesByKind().get(kind);
            Resource resource = resources.stream().filter(r -> r.getName().equals(name)).findFirst().orElse(null);
            if (resource == null) {
                continue;
            }
            JsonNode ownerReference = resource.getJsonNode().get("metadata").get("ownerReferences");
            if(ownerReference != null) {
                return getOwners(ownerReference, projectState);
            }
            owners.add(resource);
        }
        return owners;
    }

    private ResourceAction createActionForDiff(ResourceDifference resourceDifference) {
        Map<Operation, List<AttributeDifference>> differencesByOperation = resourceDifference.getAttributeDifferences().stream().filter(this::filterAttributeDifference).collect(Collectors.groupingBy(AttributeDifference::getOperation));
        List<AttributeDifference> projectOnly = differencesByOperation.get(Operation.ADD);
        List<AttributeDifference> processedOnly = differencesByOperation.get(Operation.REMOVE);
        List<AttributeDifference> updated = differencesByOperation.get(Operation.REPLACE);
        if(updated != null || processedOnly != null) {
            return new ResourceAction(ActionType.APPLY, resourceDifference.getProcessed(), updated != null ? updated : processedOnly);
        } else if(projectOnly != null) {
            return new ResourceAction(ActionType.WARNING, resourceDifference.getProject(), projectOnly);
        }
        return null;
    }

    private boolean filterAttributeDifference(AttributeDifference attributeDifference) {
        for (IgnoredAttributeIdentifier ignoredAttribute : IGNORED_ATTRIBUTES) {
            String attributeName = attributeDifference.getAttributeName();
            if (attributeName.startsWith(ignoredAttribute.getAttributeName()) || attributeName.endsWith(ignoredAttribute.getAttributeName())) {
                if (ignoredAttribute.getValue() == null || ignoredAttribute.getValue().equals(attributeDifference.getAttributeValue())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean filterIgnoredResources(ResourceDifference resourceDifference) {
        if(DifferenceType.PROJECT_ONLY.equals(resourceDifference.getType())) {
            String kind = resourceDifference.getResourceKey().getKind();
            String name = getResourceName(resourceDifference.getProject());
            String type = getResourceType(resourceDifference.getProject());
            for (IgnoredResourceIdentifier ignoredResource : IGNORED_RESOURCES) {
                String ignoredKind = ignoredResource.getKind();
                String ignoredType = ignoredResource.getType();
                String ignoredName = ignoredResource.getName();
                if((ignoredKind == null || ignoredKind.equals(kind))
                && (ignoredType == null || ignoredType.equals(type))
                && (ignoredName == null || ignoredName.equals(name))) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getResourceName(Resource resource) {
        return resource.getJsonNode().get("metadata").get("name").textValue();
    }

    private String getResourceType(Resource resource) {
        JsonNode type = resource.getJsonNode().get("type");
        return type != null ? type.textValue() : null;
    }

}
