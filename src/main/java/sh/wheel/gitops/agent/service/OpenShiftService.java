package sh.wheel.gitops.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.model.Resource;
import sh.wheel.gitops.agent.util.OpenShiftCli;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    public Map<String, List<Resource>> getAllNamespacedResources(String project) {
        List<JsonNode> allNamespacedResource = oc.getAllNamespacedResource(project);
        return allNamespacedResource.stream()
                .map(ll -> StreamSupport.stream(ll.get("items").spliterator(), false).collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .map(this::mapToResource)
                .collect(Collectors.groupingBy(Resource::getKind));
    }

    public Map<String, List<Resource>> process(Path templatePath, Map<String, String> params) {
        JsonNode process = oc.process(templatePath.toAbsolutePath().toString(), params);
        return StreamSupport.stream(process.get("items").spliterator(), false)
                .map(this::mapToResource)
                .collect(Collectors.groupingBy(Resource::getKind));
    }

}
