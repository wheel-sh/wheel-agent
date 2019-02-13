package sh.wheel.gitops.agent.repository;

import sh.wheel.gitops.agent.model.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BuildConfigDeserializer {
    public BuildConfig deserialize(InputStream newInputStream) {
        return null;
    }

    public List<BuildConfig> readDirectory(Path dir) throws IOException {
        List<Path> paths = Files.walk(dir).filter(f -> f.endsWith(".yaml")).collect(Collectors.toList());
        List<BuildConfig> configs = new ArrayList<>();
        for (Path path : paths) {
            configs.add(deserialize(Files.newInputStream(path)));
        }
        return configs;
    }

}
