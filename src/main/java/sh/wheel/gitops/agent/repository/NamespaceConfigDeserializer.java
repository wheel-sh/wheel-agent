package sh.wheel.gitops.agent.repository;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import sh.wheel.gitops.agent.config.NamespaceConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NamespaceConfigDeserializer {

    public NamespaceConfig deserialize(InputStream inputStream) {
        Yaml yaml = new Yaml(new Constructor(NamespaceConfig.class));
        return (NamespaceConfig) yaml.load(inputStream);
    }

    public List<NamespaceConfig> readDirectory(Path dir) throws IOException {
        List<Path> paths = Files.walk(dir).filter(f -> f.endsWith(".yaml")).collect(Collectors.toList());
        List<NamespaceConfig> configs = new ArrayList<>();
        for (Path path : paths) {
            configs.add(deserialize(Files.newInputStream(path)));
        }
        return configs;
    }

}
