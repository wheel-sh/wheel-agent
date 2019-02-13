package sh.wheel.gitops.agent.repository;

import sh.wheel.gitops.agent.config.TemplateConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateConfigDeserializer {

    private TemplateConfig deserialize(InputStream newInputStream) {
        return null;
    }

    public List<TemplateConfig> readDirectory(Path dir) throws IOException {
        List<Path> paths = Files.walk(dir).filter(f -> f.endsWith(".yaml")).collect(Collectors.toList());
        List<TemplateConfig> configs = new ArrayList<>();
        for (Path path : paths) {
            configs.add(deserialize(Files.newInputStream(path)));
        }
        return configs;
    }
}
