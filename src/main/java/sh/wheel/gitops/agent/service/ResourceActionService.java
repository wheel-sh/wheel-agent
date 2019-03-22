package sh.wheel.gitops.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ResourceActionService {


    private static final List<IgnoredResourceIdentifier> IGNORED_RESOURCES = new ArrayList<>();
    private static final List<IgnoredAttributeIdentifier> IGNORED_ATTRIBUTES = new ArrayList<>();

    static {
        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("Secret", "kubernetes.io/dockercfg", null));
        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("Secret", "kubernetes.io/service-account-token", null));
        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("PodMetrics", null, null));
        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("Endpoints", null, null));
        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("Event", null, null));
        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("ServiceAccount", null, "default"));
        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("ServiceAccount", null, "builder"));
        IGNORED_RESOURCES.add(new IgnoredResourceIdentifier("ServiceAccount", null, "deployer"));

        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/kind", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/apiVersion", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/apiGroup", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/status", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/metadata/creationTimestamp", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/metadata/project", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/metadata/resourceVersion", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/metadata/selfLink", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/metadata/uid", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/spec/clusterIP", null));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/spec/type", "ClusterIP"));
        IGNORED_ATTRIBUTES.add(new IgnoredAttributeIdentifier("/roleRef/apiGroup", "rbac.authorization.k8s.io"));
    }

    List<ResourceAction> createResourceActions(List<ResourceDifference> resourceDifferences) {
        return resourceDifferences.stream()
                .filter(this::filterIgnoredResources)
                .map(this::createAction)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ResourceAction createAction(ResourceDifference resourceDifference) {
        switch (resourceDifference.getPresence()) {
            case PROCESSED_ONLY:
                return new ResourceAction(ActionType.CREATE, resourceDifference.getProcessed(), resourceDifference.getAttributeDifferences());
            case NAMESPACE_ONLY:
                return createActionForProjectOnly(resourceDifference);
            case BOTH:
                return createActionForDiff(resourceDifference);
            default:
                throw new IllegalStateException("Unkown ResourcePresence " + resourceDifference.getPresence());
        }
    }

    private ResourceAction createActionForProjectOnly(ResourceDifference resourceDifference) {
        JsonNode ownerReference = resourceDifference.getCluster().getJsonNode().get("metadata").get("ownerReferences");
        if (ownerReference != null) { // don't touch owned resources
            return new ResourceAction(ActionType.IGNORE_OWNED_RESOURCE, resourceDifference.getCluster(), resourceDifference.getAttributeDifferences());
        } else {
            return new ResourceAction(ActionType.DELETE, resourceDifference.getCluster(), resourceDifference.getAttributeDifferences());
        }
    }

    private ResourceAction createActionForDiff(ResourceDifference resourceDifference) {

        Map<Operation, List<AttributeDifference>> differencesByOperation = resourceDifference.getAttributeDifferences().stream().filter(this::filterAttributeDifference).collect(Collectors.groupingBy(AttributeDifference::getOperation));
        List<AttributeDifference> projectOnly = getOrReturnEmptyList(differencesByOperation.get(Operation.REMOVE));
        List<AttributeDifference> processedOnly = getOrReturnEmptyList(differencesByOperation.get(Operation.ADD));
        List<AttributeDifference> updated = getOrReturnEmptyList(differencesByOperation.get(Operation.REPLACE));
        if (!updated.isEmpty() || !processedOnly.isEmpty()) {
            List<AttributeDifference> collect = Stream.concat(updated.stream(), processedOnly.stream()).collect(Collectors.toList());
            return new ResourceAction(ActionType.PATCH, resourceDifference.getCluster(), collect);
        } else if (!projectOnly.isEmpty()) {
            return new ResourceAction(ActionType.IGNORE_CLUSTER_ATTR, resourceDifference.getCluster(), projectOnly);
        }
        return null;
    }

    private List<AttributeDifference> getOrReturnEmptyList(List<AttributeDifference> attributeDifferences) {
        if(attributeDifferences == null) {
            return new ArrayList<>();
        }
        return attributeDifferences;
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
        if (ResourcePresence.NAMESPACE_ONLY.equals(resourceDifference.getPresence())) {
            String kind = resourceDifference.getCluster().getKind();
            String name = resourceDifference.getCluster().getName();
            String type = getResourceType(resourceDifference.getCluster());
            for (IgnoredResourceIdentifier ignoredResource : IGNORED_RESOURCES) {
                String ignoredKind = ignoredResource.getKind();
                String ignoredType = ignoredResource.getType();
                String ignoredName = ignoredResource.getName();
                if ((ignoredKind == null || ignoredKind.equals(kind))
                        && (ignoredType == null || ignoredType.equals(type))
                        && (ignoredName == null || ignoredName.equals(name))) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getResourceType(Resource resource) {
        JsonNode type = resource.getJsonNode().get("type");
        return type != null ? type.textValue() : null;
    }

}
