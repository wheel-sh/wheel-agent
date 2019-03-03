package sh.wheel.gitops.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.model.ProjectState;
import sh.wheel.gitops.agent.model.Resource;
import sh.wheel.gitops.agent.util.OpenShiftCli;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
//TODO: Change to REST...
public class OpenShiftService {

    private OpenShiftCli oc;

    public OpenShiftService() {
        this.oc = new OpenShiftCli();
    }

    public OpenShiftService(OpenShiftCli openShiftCli) {
        this.oc = openShiftCli;
    }

//    public List<Resource> getAllNamespacedResources2(String project) {
//        List<String> apiResources = oc.getApiResources(true);
//        String resources = String.join(",", apiResources);
//        JsonNode resourceList = oc.getResourceList(resources, project);
//        return StreamSupport.stream(resourceList.get("items").spliterator(), false)
//                .map(this::mapToResource)
//                .collect(Collectors.toList());
//    }

    Resource mapToResource(JsonNode jsonNode) {
        String kind = jsonNode.get("kind").textValue();
        String name = jsonNode.get("metadata").get("name").textValue();
        return new Resource(kind, name, jsonNode);
    }

    public ProjectState getProjectStateFromCluster(String projectName) {
        List<JsonNode> allResources = oc.getAllNamespacedResource(projectName);
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

    public ProjectState getProjectStateFromTemplate(Path projectTemplate, Map<String, String> projectParams, Path appTemplate, Map<String, String> appParams) {
        Map<String, List<Resource>> projectProcessed = process(projectTemplate, projectParams);
        List<Resource> projectList = projectProcessed.get("Project");
        if(projectList.size() != 1) {
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

}
