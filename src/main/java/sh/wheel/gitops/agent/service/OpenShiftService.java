package sh.wheel.gitops.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.util.OpenShiftCli;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class OpenShiftService {

    private OpenShiftCli oc;

    public OpenShiftService() {
        this.oc = new OpenShiftCli();
    }

    public OpenShiftService(OpenShiftCli openShiftCli) {
        this.oc = openShiftCli;
    }

    public List<JsonNode> getAllNamespacedResources2(String project) {
        List<String> apiResources = oc.getApiResources(true);
        String resources = apiResources.stream().collect(Collectors.joining(","));
        JsonNode resourceList = oc.getResourceList(resources, project);
        return StreamSupport.stream(resourceList.get("items").spliterator(), false).collect(Collectors.toList());
    }

    public List<JsonNode> getAllNamespacedResources(String project) {
        return oc.getAllNamespacedResource(project);
    }

    public List<JsonNode> process(Path templatePath, Map<String, String> params) {
        JsonNode process = oc.process(templatePath.toAbsolutePath().toString(), params);
        return StreamSupport.stream(process.get("items").spliterator(), false).collect(Collectors.toList());
    }

}
