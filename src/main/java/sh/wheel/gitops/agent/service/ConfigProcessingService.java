package sh.wheel.gitops.agent.service;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.config.AppConfig;
import sh.wheel.gitops.agent.config.NamespaceConfig;
import sh.wheel.gitops.agent.config.ParameterConfig;
import sh.wheel.gitops.agent.model.App;
import sh.wheel.gitops.agent.model.Group;
import sh.wheel.gitops.agent.model.NamespaceState;
import sh.wheel.gitops.agent.model.WheelRepository;
import sh.wheel.gitops.agent.util.ReplaceValueStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ConfigProcessingService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private OpenShiftClient client;

    @Autowired
    public ConfigProcessingService(OpenShiftClient client) {
        this.client = client;
    }

    public Map<String, NamespaceState> processExpectedNamespaceStatesByName(WheelRepository repository) {
        Map<String, NamespaceState> nsStateByName = new HashMap<>();
        for (App app : repository.getApps().values()) {
            try {
                Group group = lookupGroup(app.getAppConfig(), repository.getGroups());
                for (NamespaceConfig namespaceConfig : app.getNamespaceConfigs()) {
                    Path templatePath = app.getAppDir().resolve("template").resolve(namespaceConfig.getTemplateFile());
                    String nsName = namespaceConfig.getName();
                    if(!Files.exists(templatePath)) {
                        throw new IllegalStateException(String.format("Cannot find template %s for app %s in namespace %s", templatePath.toString(), app.getAppConfig().getName(), nsName));
                    }
                    Map<String, String> params = namespaceConfig.getParameters().stream().collect(Collectors.toMap(ParameterConfig::getName, ParameterConfig::getValue));
                    List<HasMetadata> processTemplate = processTemplate(templatePath, params);
                    List<HasMetadata> quotaAndLimitRangeObjects = getQuotaAndLimitRangeObjects(app, group);

                    Map<String, List<HasMetadata>> objectsByKind = processTemplate.stream().collect(Collectors.groupingBy(HasMetadata::getKind));
                    nsStateByName.put(nsName, new NamespaceState(nsName, objectsByKind));
                }
            } catch (Exception e) {
                LOG.error("Could not process app "+app.getAppConfig().getName(), e);
            }
        }
        return null;
    }

    private Group lookupGroup(AppConfig appConfig, Map<String, Group> groups) {
        Group group = groups.get(appConfig.getGroup());
        if(group == null) {
            throw new IllegalStateException("Group '"+appConfig.getGroup()+"' configured in '"+appConfig.getName()+"' not found");
        }
        return group;
    }

    List<HasMetadata> processTemplate(Path templatePath, Map<String, String> params) throws FileNotFoundException {
        InputStream is = ReplaceValueStream.replaceValues(new FileInputStream(templatePath.toFile()), params);
        return client.templates().load(is).processLocally(params).getItems();
    }

    List<HasMetadata> getQuotaAndLimitRangeObjects(App app, Group group) {

        return new ArrayList<>();
    }

}
