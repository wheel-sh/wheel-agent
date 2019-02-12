package sh.wheel.gitops.agent.repository;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import sh.wheel.gitops.agent.config.GroupConfig;

import java.io.InputStream;

public class GroupConfigDeserializer {

    public GroupConfig deserialize(InputStream inputStream) {
        Yaml yaml = new Yaml(new Constructor(GroupConfig.class));
        return (GroupConfig) yaml.load(inputStream);
    }

}
