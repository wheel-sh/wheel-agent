package sh.wheel.gitops.agent.util;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.utils.IOHelpers;
import io.fabric8.kubernetes.client.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Replaces template parameter values in the stream to avoid
 * parsing issues of templates with numeric expressions
 */
public class ReplaceValueStream {
    private final Map<String, String> valuesMap;

    /**
     * Returns a stream with the template parameter expressions replaced
     */
    public static InputStream replaceValues(InputStream is, Map<String, String> valuesMap) {
        return new ReplaceValueStream(valuesMap).createInputStream(is);
    }

    public ReplaceValueStream(Map<String, String> valuesMap) {
        this.valuesMap = valuesMap;
    }

    public InputStream createInputStream(InputStream is) {
        try {
            String json = IOHelpers.readFully(is);
            String replaced = replaceValues(json);
            return new ByteArrayInputStream(replaced.getBytes());
        } catch (IOException e) {
            throw KubernetesClientException.launderThrowable(e);
        }
    }

    private String replaceValues(String json) {
        String answer = json;
        for (Map.Entry<String, String> entry : valuesMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            answer = Utils.replaceAllWithoutRegex(answer, "${" + key + "}", value);
            answer = Utils.replaceAllWithoutRegex(answer, "${{" + key + "}}", value);
        }
        return answer;
    }
}