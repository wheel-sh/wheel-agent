package sh.wheel.gitops.agent.okd;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.openshift.client.OpenShiftClient;
import sh.wheel.gitops.agent.util.ReplaceValueStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class LocalTemplateProcessor {

    public List<HasMetadata> getProcessedTemplateItems(Path path, Map<String, String> params, OpenShiftClient client) throws FileNotFoundException {
        InputStream is = ReplaceValueStream.replaceValues(new FileInputStream(path.toFile()), params);
        return client.templates().load(is).processLocally(params).getItems();
    }

}
