package sh.wheel.gitops.agent.okd;

import com.openshift.internal.restclient.model.template.Template;
import com.openshift.restclient.ClientBuilder;
import com.openshift.restclient.IClient;
import com.openshift.restclient.capability.CapabilityVisitor;
import com.openshift.restclient.capability.server.ITemplateProcessing;
import com.openshift.restclient.model.IResource;
import com.openshift.restclient.model.template.ITemplate;
import org.jboss.dmr.ModelNode;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Disabled
public class OpenShiftClientSnippetsTest {

    private static final Logger LOG = LoggerFactory.getLogger(OpenShiftClientSnippetsTest.class);

    @Test
    void process() throws Exception {
        IClient client = new ClientBuilder("")
                .withUserName("")
                .build();
        client.getAuthorizationContext().setToken("");


        String yaml = new String(Files.readAllBytes(Paths.get(getClass().getResource("/valid-test-data/apps/example-app/template/app.v1.yaml").toURI())));
        Object load = new Yaml().load(yaml);
        String templateJson = new JSONObject((Map<String, Object>) load).toString();
        ModelNode node = ModelNode.fromJSONString(templateJson);

        final Template template = new Template(node, client, null);
        template.updateParameter("REPLICA_COUNT", "1");
        template.updateParameter("IMAGE_NAME", "bitnami/nginx");
        template.updateParameter("IMAGE_VERSION", "1.14-ol-7");
        template.setNamespace(null);

        final Collection<IResource> results = new ArrayList<IResource>();
        client.accept(new CapabilityVisitor<ITemplateProcessing, Object>() {

            @Override
            public Object visit(ITemplateProcessing capability) {

                LOG.debug("Processing template: {}", template.toJson());
                assertFalse(template.getObjects().isEmpty());
                final int items = template.getObjects().size();
                ITemplate processedTemplate = capability.process(template, "demo");
                LOG.debug("Applying template: {}", processedTemplate.toJson());
                LOG.debug("Applied template");
                assertEquals(items, template.getObjects().size());
                for (IResource resource : processedTemplate.getObjects()) {
                    LOG.debug("creating: {}", resource);
                    results.add(client.create(resource, "demo"));
                    LOG.debug("created: {}", resource.toJson());
                }
                return null;
            }
        }, new Object());

        System.out.println();
    }

}
