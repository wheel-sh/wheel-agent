package sh.wheel.gitops.agent.repository;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import sh.wheel.gitops.agent.config.NamespaceConfig;

import java.io.InputStream;

public class NamespaceConfigDeserializer {

    public NamespaceConfig deserialize(InputStream inputStream) {
        Yaml yaml = new Yaml(new Constructor(NamespaceConfig.class));
        return (NamespaceConfig) yaml.load(inputStream);
    }

}
