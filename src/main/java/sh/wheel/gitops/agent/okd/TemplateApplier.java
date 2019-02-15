package sh.wheel.gitops.agent.okd;

import com.openshift.restclient.IClient;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.capability.resources.IProjectTemplateProcessing;
import com.openshift.restclient.model.IResource;
import com.openshift.restclient.model.template.ITemplate;

import java.util.Collection;

public class TemplateApplier {

    public Collection<IResource> apply(ITemplate template, String namespace, IClient client) {
        IResource project = client.get(ResourceKind.PROJECT, namespace, "");
        IProjectTemplateProcessing processing = project.getCapability(IProjectTemplateProcessing.class);
        return processing.apply(template);
    }

}
