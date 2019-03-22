package sh.wheel.gitops.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.model.*;
import sh.wheel.gitops.agent.util.OpenShiftRestClient;
import sh.wheel.gitops.agent.util.OpenShiftTemplateUtil;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OpenShiftService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectMapper OM = new ObjectMapper();

    private final OpenShiftTemplateUtil templateUtil;
    private final OpenShiftRestClient openShiftRestClient;
    private final List<String> requiredVerbs = Arrays.asList("create", "delete", "get", "list", "patch", "update", "watch");
    private List<ApiResource> availableApiResources;
    private String whoAmI;

    public OpenShiftService(OpenShiftTemplateUtil templateUtil, OpenShiftRestClient openShiftRestClient) {
        this.templateUtil = templateUtil;
        this.openShiftRestClient = openShiftRestClient;
    }

    @PostConstruct
    public void init() {
        whoAmI = openShiftRestClient.whoAmI();
        availableApiResources = getManageableResources();
    }

    List<ProjectState> getProjectStatesFromCluster() {
        long start = System.currentTimeMillis();
        List<ProjectState> collect = openShiftRestClient.getAllProjects()
                .stream().filter(p -> whoAmI.equals(getRequester(p)))
                .map(mp -> mp.get("metadata").get("name").textValue())
                .map(this::getProjectStateFromCluster)
                .collect(Collectors.toList());
        LOG.debug("Time to fetch project states (" + collect.size() + ") from cluster: " + (System.currentTimeMillis() - start) + "ms");
        return collect;
    }

    private String getRequester(JsonNode p) {
        JsonNode jsonNode = p.get("metadata").get("annotations");
        if (jsonNode != null) {
            JsonNode requestorNode = jsonNode.get("openshift.io/requester");
            if (requestorNode != null) {
                return requestorNode.textValue();
            }
        }
        return null;
    }

    ProjectState getProjectStateFromCluster(String projectName) {
        List<ApiResource> apiResources = openShiftRestClient.fetchManageableResources(whoAmI, projectName, requiredVerbs, availableApiResources);
        Map<ResourceKey, Resource> resourceByKey = openShiftRestClient.fetchResourcesFromNamespace(apiResources, projectName).stream().collect(Collectors.toMap(Resource::getResourceKey, Function.identity()));
        Resource project = openShiftRestClient.fetchProject(projectName);
        return new ProjectState(project, resourceByKey);
    }

    List<ApiResource> getManageableResources() {
        return openShiftRestClient.getFilteredApiResources(true, requiredVerbs);
    }

    ProjectState getProjectStateFromTemplate(Path projectTemplate, Map<String, String> projectParams, Path appTemplate, Map<String, String> appParams) {
        ResourceKey projectKey = ResourceKey.projectWithName(projectParams.get("PROJECT_NAME"));
        Map<ResourceKey, Resource> projectProcessed = process(projectTemplate, projectParams);
        Resource project = projectProcessed.get(projectKey);
        projectProcessed.remove(projectKey);
        Map<ResourceKey, Resource> appProcessed = process(appTemplate, appParams);
        Map<ResourceKey, Resource> resources = Stream.concat(projectProcessed.entrySet().stream(), appProcessed.entrySet().stream()).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue));
        return new ProjectState(project, resources);
    }


    Map<ResourceKey, Resource> process(Path templatePath, Map<String, String> params) {
        List<Resource> process = templateUtil.process(templatePath, params);
        return process.stream().collect(Collectors.toMap(this::createResourceKey, Function.identity()));
    }

    private ResourceKey createResourceKey(Resource resource) {
        return new ResourceKey(resource.getName(), resource.getKind(), resource.getApiVersion());
    }

    String getWhoAmI() {
        return whoAmI;
    }

    public void patch(Resource resource, List<AttributeDifference> attributeDifferences, String projectName) {
        ArrayNode patchNodes = OM.createArrayNode();
        attributeDifferences.stream().map(AttributeDifference::getDiff).forEach(patchNodes::addPOJO);
        openShiftRestClient.patchResource(resource, patchNodes);
        LOG.info(String.format("Applied resource %s/%s in namespace %s", resource.getKind(), resource.getName(), projectName));
    }

    void newProject(String projectName) {
        openShiftRestClient.newProject(projectName);
    }

    void createNamespacedResource(Resource resource, String namespace) {
        ApiResource apiResource = resolveApiResource(resource);
        openShiftRestClient.createNamespacedResource(resource, apiResource, namespace);
        LOG.info(String.format("Created resource %s/%s in namespace %s", apiResource.getName(), resource.getName(), namespace));
    }

    private ApiResource resolveApiResource(Resource resource) {
        return availableApiResources.stream().filter(ai -> ai.getKind().equals(resource.getKind()) &&
                ai.getApiVersion().equals(resource.getApiVersion()) &&
                ai.getKind().equals(resource.getResourceKey().getKind())).findAny().orElseThrow(() -> new IllegalStateException("No api resource found for key :" + resource.getResourceKey()));
    }

    public void delete(Resource clusterResource) {
        LOG.info(String.format("Deleted resource %s", clusterResource.getJsonNode().get("metadata").get("selfLink").textValue()));
        openShiftRestClient.delete(clusterResource);
    }
}
