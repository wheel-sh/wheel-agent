package sh.wheel.gitops.agent.okd;

import com.openshift.internal.restclient.model.template.Template;
import com.openshift.restclient.IClient;
import com.openshift.restclient.capability.server.ITemplateProcessing;
import com.openshift.restclient.model.template.ITemplate;
import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.wheel.gitops.agent.config.ParameterConfig;
import sh.wheel.gitops.agent.util.YamlUtil;

import java.util.List;

public class TemplateProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateProcessor.class);

    public ITemplate process(String templateStr, List<ParameterConfig> templateParameters, String namespace, IClient client) {
        ModelNode node = YamlUtil.toModelNode(templateStr);
        Template template = createTemplateAndApplyParameters(node, templateParameters, client);
        return processTemplate(template, client, namespace);
    }

    private ITemplate processTemplate(Template template, IClient client, String namespace) {
        if (client.supports(ITemplateProcessing.class)) {
            ITemplateProcessing templateProcessing = client.getCapability(ITemplateProcessing.class);
            return templateProcessing.process(template, namespace);
        }
        throw new UnsupportedOperationException();
    }


    private Template createTemplateAndApplyParameters(ModelNode node, List<ParameterConfig> parameters, IClient client) {
        final Template template = new Template(node, client, null);
        for (ParameterConfig parameter : parameters) {
            template.updateParameter(parameter.getName(), parameter.getValue());
        }
        return template;
    }

}
