package sh.wheel.gitops.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WheelAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(WheelAgentApplication.class, args);
    }


    //        OpenShiftClient client = new DefaultOpenShiftClient();
//        Path appsPath = repositoryPath.resolve("apps");
//        List<App> apps = new AppsConfigReader().readAllApps(appsPath);
//        for (App app : apps) {
//            for (NamespaceConfig namespaceConfig : app.getNamespaceConfigs()) {
//                String templateFile = namespaceConfig.getTemplateFile();
//                Path tplPath = app.getAppDir().resolve(TEMPLATE_DIR).resolve(templateFile);
//                Map<String, String> params = namespaceConfig.getParameters().stream().collect(Collectors.toMap(ParameterConfig::getName, ParameterConfig::getValue));
//                if (client.projects().withName(namespaceConfig.getName()).getResourceList() == null) {
//                    Project project = client.projects().create(new ProjectBuilder()
//                            .withNewMetadata()
//                            .withName(namespaceConfig.getName()).endMetadata()
//                            .build());
//                }
//                List<HasMetadata> processedTemplateItems = new LocalTemplateProcessor().processTemplate(tplPath, params, client);
//                Map<String, List<HasMetadata>> projectResources = new ProjectResourceLoader().getProjectStateFromCluster(namespaceConfig.getName(), client);
//                for (HasMetadata processedTemplateItem : processedTemplateItems) {
//                    List<HasMetadata> projectResourceList = projectResources.getResourceList(processedTemplateItem.getKind());
//                    boolean create = true;
//                    if (projectResourceList != null && projectResourceList.size() > 0) {
//                        HasMetadata projectResource = projectResourceList.stream().filter(pr -> pr.getMetadata().getName().equals(processedTemplateItem.getMetadata().getName())).findFirst().getResourceList();
//                        List<JsonNode> jsonNodes = new ResourceDifferenceEvaluator().evaluateDiff(processedTemplateItem, projectResource);
//                        create = jsonNodes.size() > 0;
//                    }
//                    if (create) {
//                        HasMetadata orReplace = client.resource(processedTemplateItem).inNamespace(namespaceConfig.getName()).createOrReplace();
//                    }
//                }
//            }
//        }

}
