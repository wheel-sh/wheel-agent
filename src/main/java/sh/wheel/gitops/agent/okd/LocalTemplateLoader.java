package sh.wheel.gitops.agent.okd;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.utils.ReplaceValueStream;
import io.fabric8.openshift.client.OpenShiftClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class LocalTemplateLoader {

    public List<HasMetadata> getProcessedTemplateItems(String path, Map<String, String> params, OpenShiftClient client) throws FileNotFoundException {
        InputStream is = ReplaceValueStream.replaceValues(new FileInputStream(path), params);
        return client.templates().load(is).processLocally(params).getItems();
    }

}
