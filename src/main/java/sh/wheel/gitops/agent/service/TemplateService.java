package sh.wheel.gitops.agent.service;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.util.ReplaceValueStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
public class TemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private OpenShiftClient client;

    @Autowired
    public TemplateService(OpenShiftClient client) {
        this.client = client;
    }

    public List<HasMetadata> processTemplate(Path templatePath, Map<String, String> params) throws FileNotFoundException {
        InputStream is = ReplaceValueStream.replaceValues(new FileInputStream(templatePath.toFile()), params);
        return client.templates().load(is).processLocally(params).getItems();
    }

}
