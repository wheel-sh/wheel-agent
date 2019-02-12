package sh.wheel.gitops.agent.repository;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import sh.wheel.gitops.agent.config.AppConfig;

import java.io.InputStream;

public class AppConfigDeserializer {

    public AppConfig deserialize(InputStream inputStream) {
        Yaml yaml = new Yaml(new Constructor(AppConfig.class));
        return yaml.load(inputStream);
    }

}
