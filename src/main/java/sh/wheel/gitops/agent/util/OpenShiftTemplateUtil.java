package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import sh.wheel.gitops.agent.model.Resource;
import sh.wheel.gitops.agent.model.ResourceKey;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class OpenShiftTemplateUtil {

    private final ObjectReader objectReader;

    public OpenShiftTemplateUtil(ObjectReader objectReader) {
        this.objectReader = objectReader;
    }

    public static OpenShiftTemplateUtil create() {
        return new OpenShiftTemplateUtil(new ObjectMapper(new YAMLFactory()).reader());
    }

    public List<Resource> process(Path templatePath, Map<String, String> params) {
        try {
            FileInputStream is = new FileInputStream(templatePath.toFile());
            InputStream processedIs = ReplaceValueStream.replaceValues(is, params);
            JsonNode jsonNode = objectReader.readTree(processedIs);
            return StreamSupport.stream(jsonNode.get("objects").spliterator(), false)
                    .map(this::mapToResource)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Resource mapToResource(JsonNode jsonNode) {
        String kind = jsonNode.get("kind").textValue();
        String name = jsonNode.get("metadata").get("name").textValue();
        String apiVersion = jsonNode.get("apiVersion").textValue();
        return new Resource(new ResourceKey(name, kind, apiVersion), null, jsonNode);
    }

}
