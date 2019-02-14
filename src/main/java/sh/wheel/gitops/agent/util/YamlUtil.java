package sh.wheel.gitops.agent.util;

import org.jboss.dmr.ModelNode;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class YamlUtil {
    public static String toJson(String yaml) {
        Object load = new Yaml().load(yaml);
        return new JSONObject((Map<String, Object>) load).toString();
    }

    public static ModelNode toModelNode(String yaml) {
        return ModelNode.fromJSONString(toJson(yaml));
    }
}
