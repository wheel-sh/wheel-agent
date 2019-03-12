package sh.wheel.gitops.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.model.ProjectState;
import sh.wheel.gitops.agent.model.Resource;
import sh.wheel.gitops.agent.util.OpenShiftCli;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
//TODO: Change to REST...
public class OpenShiftService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private OpenShiftCli oc;
    private List<String> requiredOperations = Arrays.asList("create", "delete", "get", "list", "patch", "update", "watch");
    private Set<String> apiResources;

    public OpenShiftService() {
        this.oc = new OpenShiftCli();
    }

    public OpenShiftService(OpenShiftCli openShiftCli) {
        this.oc = openShiftCli;
    }

    @PostConstruct
    public void init() {
        // TODO: Analyze why events is in there twice?
        apiResources = getManageableResources();
    }

    Resource mapToResource(JsonNode jsonNode) {
        String kind = jsonNode.get("kind").textValue();
        String apiVersion = jsonNode.get("apiVersion").textValue();
        String name = jsonNode.get("metadata").get("name").textValue();
        return new Resource(kind, name, apiVersion, jsonNode);
    }

    public List<ProjectState> getProjectStatesFromCluster() {
        return oc.getManageableProjects().parallelStream()
                .map(mp -> mp.get("metadata").get("name").textValue())
                .filter(Objects::nonNull)
                .map(this::getProjectStateFromCluster)
                .collect(Collectors.toList());
    }

    public ProjectState getProjectStateFromCluster(String projectName) {
        List<JsonNode> allResources = oc.getResources(projectName, apiResources);
        Map<String, List<Resource>> projectResources = allResources.stream()
                .map(ll -> StreamSupport.stream(ll.get("items").spliterator(), false).collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .map(this::mapToResource)
                .collect(Collectors.groupingBy(Resource::getKind));
        JsonNode projectList = oc.getResource("project", projectName, projectName);

        Resource projectResource = mapToResource(projectList);
        projectResources.put("Project", Collections.singletonList(projectResource));
        return new ProjectState(projectName, projectResources);
    }

    Set<String> getManageableResources() {
        List<String> apiResourcesWide = oc.getApiResourcesWide(true);
        Set<String> result = new HashSet<>();
        for (String detail : apiResourcesWide) {
            String[] split = detail.split("\\s+");
            String resourceName = split[0];
            String operations = detail.substring(detail.indexOf("[") + 1, detail.indexOf("]"));
            List<String> givenOperations = Arrays.asList(operations.split(" "));
            boolean hasAllRequiredOperations = givenOperations.containsAll(requiredOperations);
            if (hasAllRequiredOperations && oc.canI("get", resourceName)) {
                result.add(resourceName);
            }
        }
        return result;
    }

    public ProjectState getProjectStateFromTemplate(Path projectTemplate, Map<String, String> projectParams, Path appTemplate, Map<String, String> appParams) {
        Map<String, List<Resource>> projectProcessed = process(projectTemplate, projectParams);
        List<Resource> projectList = projectProcessed.get("Project");
        if (projectList.size() != 1) {
            throw new IllegalStateException("Project size is not 1 (" + projectList.size() + ")");
        }
        Resource project = projectList.get(0);
        Map<String, List<Resource>> appProcessed = process(appTemplate, appParams);
        Map<String, List<Resource>> resources = Stream.concat(projectProcessed.entrySet().stream(), appProcessed.entrySet().stream()).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue));
        return new ProjectState(project.getName(), resources);
    }


    public Map<String, List<Resource>> process(Path templatePath, Map<String, String> params) {
        JsonNode process = oc.process(templatePath.toAbsolutePath().toString(), params);
        return StreamSupport.stream(process.get("items").spliterator(), false)
                .map(this::mapToResource)
                .collect(Collectors.groupingBy(Resource::getKind));
    }

    public String getWhoAmI() {
        return oc.getWhoAmI();
    }

    public void apply(String projectName, Resource resource) {
        LOG.info(String.format("Applied resource %s/%s in project %s", resource.getKind(), resource.getName(), projectName));
        oc.apply(projectName, resource.getJsonNode());
    }

    public void newProject(String projectName) {
        oc.newProject(projectName);
    }

    public void delete(String projectName, Resource resource) {
        oc.delete(projectName, resource.getKind(), resource.getName());
    }
}
