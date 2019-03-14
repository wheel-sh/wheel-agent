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
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
//TODO: Change to REST...
public class OpenShiftService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private OpenShiftTemplateUtil templateUtil;
    private OpenShiftRestClient openShiftRestClient;
    private List<String> requiredVerbs = Arrays.asList("create", "delete", "get", "list", "patch", "update", "watch");
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


    Resource mapToResource(JsonNode jsonNode) {
        String kind = jsonNode.get("kind").textValue();
        String apiVersion = jsonNode.get("apiVersion").textValue();
        String name = jsonNode.get("metadata").get("name").textValue();
        JsonNode uidNode = jsonNode.get("metadata").get("uid");
        String uid = null;
        if (uidNode != null) {
            uid = uidNode.textValue();
        }
        return new Resource(new ResourceKey(name, kind, apiVersion), uid, jsonNode);
    }

    public List<ProjectState> getProjectStatesFromCluster() {
        long start = System.currentTimeMillis();
        List<CompletableFuture<ProjectState>> projectRequests = openShiftRestClient.getAllProjects()
                .stream().filter(p -> whoAmI.equals(getRequester(p)))
                .map(mp -> mp.get("metadata").get("name").textValue())
                .map(p -> CompletableFuture.supplyAsync(() -> getProjectStateFromCluster(p)))
                .collect(Collectors.toList());
        List<ProjectState> collect = projectRequests.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        LOG.info("Time to fetch project states (" + collect.size() + ") from cluster: " + (System.currentTimeMillis() - start) + "ms");
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

    public ProjectState getProjectStateFromCluster(String projectName) {
        List<ApiResource> apiResources = openShiftRestClient.fetchManageableResources(whoAmI, projectName, requiredVerbs, availableApiResources);
        Map<ResourceKey, Resource> resourceByKey = openShiftRestClient.fetchResourcesFromNamespace(apiResources, projectName).stream().collect(Collectors.toMap(Resource::getResourceKey, Function.identity()));
        Resource project = openShiftRestClient.fetchProject(projectName);
        return new ProjectState(project, resourceByKey);
    }

    List<ApiResource> getManageableResources() {
        return openShiftRestClient.getFilteredApiResources(true, requiredVerbs);
    }

    public ProjectState getProjectStateFromTemplate(Path projectTemplate, Map<String, String> projectParams, Path appTemplate, Map<String, String> appParams) {
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


    public Map<ResourceKey, Resource> process(Path templatePath, Map<String, String> params) {
        List<Resource> process = templateUtil.process(templatePath, params);
        return process.stream().collect(Collectors.toMap(this::createResourceKey, Function.identity()));
    }

    private ResourceKey createResourceKey(Resource resource) {
        return new ResourceKey(resource.getName(), resource.getKind(), resource.getApiVersion());
    }

    public String getWhoAmI() {
        return whoAmI;
    }

    public void patch(Resource resource, List<AttributeDifference> attributeDifferences, String projectName) {
        ApiResource apiResource = resolveApiResource(resource);
        String endpoint = apiResource.getApiEndpoint(projectName) + "/" + resource.getName();
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode patchNodes = objectMapper.createArrayNode();
        attributeDifferences.stream().map(AttributeDifference::getDiff).forEach(patchNodes::addPOJO);
        JsonNode jsonNode = openShiftRestClient.patchResource(resource, patchNodes);
        LOG.info(String.format("Applied resource %s/%s in namespace %s", resource.getKind(), resource.getName(), projectName));
    }

    public void newProject(String projectName) {
        openShiftRestClient.newProject(projectName);
    }

    public JsonNode createNamespacedResource(Resource resource, String namespace) {
        ApiResource apiResource = resolveApiResource(resource);
        JsonNode namespacedResource = openShiftRestClient.createNamespacedResource(resource, apiResource, namespace);
        LOG.info(String.format("Created resource %s/%s in namespace %s", apiResource.getName(), resource.getName(), namespace));
        return namespacedResource;
    }

    private ApiResource resolveApiResource(Resource resource) {
        return availableApiResources.stream().filter(ai -> ai.getKind().equals(resource.getKind()) &&
                ai.getApiVersion().equals(resource.getApiVersion()) &&
                ai.getKind().equals(resource.getKind())).findAny().orElseThrow(() -> new IllegalStateException("No api resource found for key :" + resource.getResourceKey()));
    }

    public void delete(Resource clusterResource) {
        LOG.info(String.format("Deleted resource %s", clusterResource.getJsonNode().get("metadata").get("selfLink").textValue()));
        openShiftRestClient.delete(clusterResource);
    }
}
