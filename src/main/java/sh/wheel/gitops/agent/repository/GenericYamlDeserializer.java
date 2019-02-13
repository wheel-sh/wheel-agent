package sh.wheel.gitops.agent.repository;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GenericYamlDeserializer {

    public <T> T deserialize(Path yamlFile, Class<T> type) throws IOException {
        Yaml yaml = new Yaml(new Constructor(type));
        InputStream io = Files.newInputStream(yamlFile);
        return (T) yaml.load(io);
    }

    public <T> List<T> readDirectory(Path dir, Class<T> type) throws IOException {
        List<Path> paths = Files.list(dir)
                .filter(f -> f.toString().endsWith(".yaml") || f.toString().endsWith(".yml"))
                .collect(Collectors.toList());
        List<T> configs = new ArrayList<>();
        for (Path path : paths) {
            T deserialize = deserialize(path, type);
            configs.add(deserialize);
        }
        return configs;
    }

}
