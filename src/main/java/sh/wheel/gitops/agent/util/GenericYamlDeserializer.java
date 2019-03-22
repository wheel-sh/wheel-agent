package sh.wheel.gitops.agent.util;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenericYamlDeserializer {

    @SuppressWarnings("unchecked")
    public <T> T deserialize(Path yamlFile, Class<T> type) throws IOException {
        Yaml yaml = new Yaml(new Constructor(type));
        InputStream io = Files.newInputStream(yamlFile);
        return (T) yaml.load(io);
    }

    public <T> Map<String, T> readDirectory(Path dir, Class<T> type) throws IOException {
        List<Path> paths = Files.list(dir)
                .filter(f -> f.toString().endsWith(".yaml") || f.toString().endsWith(".yml"))
                .collect(Collectors.toList());
        Map<String, T> envByFileName = new HashMap<>();
        for (Path path : paths) {
            String fileName = path.getFileName().toString();
            String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf(".y"));
            T deserialize = deserialize(path, type);
            envByFileName.put(fileNameWithoutExtension, deserialize);
        }
        return envByFileName;
    }

}
