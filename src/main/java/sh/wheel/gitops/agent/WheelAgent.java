package sh.wheel.gitops.agent;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DoneableDeploymentConfig;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.ProjectBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.dsl.DeployableScalableResource;
import sh.wheel.gitops.agent.config.NamespaceConfig;
import sh.wheel.gitops.agent.config.ParameterConfig;
import sh.wheel.gitops.agent.model.App;
import sh.wheel.gitops.agent.okd.LocalTemplateProcessor;
import sh.wheel.gitops.agent.okd.ProjectResourceLoader;
import sh.wheel.gitops.agent.okd.ResourceDifferenceEvaluator;
import sh.wheel.gitops.agent.repository.AppsConfigReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static sh.wheel.gitops.agent.repository.AppsConfigReader.TEMPLATE_DIR;

public class WheelAgent {


    public void start(Path repositoryPath) throws IOException {
        OpenShiftClient client = new DefaultOpenShiftClient();
        Path appsPath = repositoryPath.resolve("apps");
        List<App> apps = new AppsConfigReader().readAllApps(appsPath);
        for (App app : apps) {
            for (NamespaceConfig namespaceConfig : app.getNamespaceConfigs()) {
                String templateFile = namespaceConfig.getTemplateFile();
                Path tplPath = app.getAppDir().resolve(TEMPLATE_DIR).resolve(templateFile);
                Map<String, String> params = namespaceConfig.getParameters().stream().collect(Collectors.toMap(ParameterConfig::getName, ParameterConfig::getValue));
                if (client.projects().withName(namespaceConfig.getName()).get() == null) {
                    Project project = client.projects().create(new ProjectBuilder()
                            .withNewMetadata()
                            .withName(namespaceConfig.getName()).endMetadata()
                            .build());
                }
                List<HasMetadata> processedTemplateItems = new LocalTemplateProcessor().getProcessedTemplateItems(tplPath, params, client);
                Map<String, List<HasMetadata>> projectResources = new ProjectResourceLoader().loadAll(namespaceConfig.getName(), client);
                for (HasMetadata processedTemplateItem : processedTemplateItems) {
                    List<HasMetadata> projectResourceList = projectResources.get(processedTemplateItem.getKind());
                    boolean create = true;
                    if (projectResourceList != null && projectResourceList.size() > 0) {
                        HasMetadata projectResource = projectResourceList.stream().filter(pr -> pr.getMetadata().getName().equals(processedTemplateItem.getMetadata().getName())).findFirst().get();
                        List<JsonNode> jsonNodes = new ResourceDifferenceEvaluator().evaluateDiff(processedTemplateItem, projectResource);
                        create = jsonNodes.size() > 0;
                    }
                    if (create) {
                        HasMetadata orReplace = client.resource(processedTemplateItem).inNamespace(namespaceConfig.getName()).createOrReplace();
                    }
                }
            }
        }
    }

}
