package sh.wheel.gitops.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.OpenShiftRestClientFactory;
import sh.wheel.gitops.agent.model.ApiResource;
import sh.wheel.gitops.agent.model.ProjectState;
import sh.wheel.gitops.agent.model.Resource;
import sh.wheel.gitops.agent.model.ResourceKey;
import sh.wheel.gitops.agent.util.OpenShiftCli;
import sh.wheel.gitops.agent.util.OpenShiftRestClient;
import sh.wheel.gitops.agent.util.OpenShiftTemplateUtil;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
//TODO: Change to REST...
public class OpenShiftService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private OpenShiftCli oc;
    private OpenShiftTemplateUtil templateUtil;
    private OpenShiftRestClient openShiftRestClient;
    private List<String> requiredVerbs = Arrays.asList("create", "delete", "get", "list", "patch", "update", "watch");
    private List<ApiResource> availableApiResources;
    private String whoAmI;

    public OpenShiftService() {
        this.oc = new OpenShiftCli();
        this.templateUtil = OpenShiftTemplateUtil.create();
        this.openShiftRestClient = new OpenShiftRestClientFactory().createOpenShiftRestClient();

    }

    public OpenShiftService(OpenShiftCli openShiftCli, OpenShiftTemplateUtil templateUtil, OpenShiftRestClient openShiftRestClient) {
        this.oc = openShiftCli;
        this.templateUtil = templateUtil;
        this.openShiftRestClient = openShiftRestClient;
    }

    @PostConstruct
    public void init() {
        // TODO: Analyze why events is in there twice?
        whoAmI = openShiftRestClient.whoAmI();
        availableApiResources = getManageableResources();
    }


    Resource mapToResource(JsonNode jsonNode) {
        String kind = jsonNode.get("kind").textValue();
        String apiVersion = jsonNode.get("apiVersion").textValue();
        String name = jsonNode.get("metadata").get("name").textValue();
        JsonNode uidNode = jsonNode.get("metadata").get("uid");
        String uid = null;
        if(uidNode != null) {
            uid = uidNode.textValue();
        }
        return new Resource(new ResourceKey(name, kind, apiVersion), uid, jsonNode);
    }

    public List<ProjectState> getProjectStatesFromCluster() {
        return oc.getManageableProjects().parallelStream()
                .map(mp -> mp.get("metadata").get("name").textValue())
                .filter(Objects::nonNull)
                .map(this::getProjectStateFromCluster)
                .collect(Collectors.toList());
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
        return oc.getWhoAmI();
    }

    public void apply(Resource resource, String projectName) {
        LOG.info(String.format("Applied resource %s/%s in project %s", resource.getKind(), resource.getName(), projectName));
        oc.apply(projectName, resource.getJsonNode());
    }

    public void newProject(String projectName) {
        oc.newProject(projectName);
    }

    public void delete(ResourceKey key, String namespace) {
        openShiftRestClient.delete(key, namespace);
    }
}
