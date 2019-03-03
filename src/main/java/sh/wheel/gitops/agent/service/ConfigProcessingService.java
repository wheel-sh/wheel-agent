package sh.wheel.gitops.agent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.config.AppConfig;
import sh.wheel.gitops.agent.model.Group;
import sh.wheel.gitops.agent.model.ProjectState;
import sh.wheel.gitops.agent.model.WheelRepository;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
public class ConfigProcessingService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private OpenShiftService openShiftService;

    @Autowired
    public ConfigProcessingService(OpenShiftService openShiftClient) {
        this.openShiftService = openShiftClient;
    }

    public List<ProjectState> processExpectedNamespaceStates(WheelRepository repository) {
        List<ProjectState> projectStates = new ArrayList<>();
//        for (App app : repository.getApps().values()) {
//            try {
//                Group group = lookupGroup(app.getAppConfig(), repository.getGroups());
//                for (NamespaceConfig namespaceConfig : app.getNamespaceConfigs()) {
//                    Path templatePath = app.getAppDir().resolve("template").resolve(namespaceConfig.getTemplateFile());
//                    String nsName = namespaceConfig.getName();
//                    if(!Files.exists(templatePath)) {
//                        throw new IllegalStateException(String.format("Cannot find template %s for app %s in namespace %s", templatePath.toString(), app.getAppConfig().getName(), nsName));
//                    }
//                    Map<String, String> params = namespaceConfig.getParameters().stream().collect(Collectors.toMap(ParameterConfig::getName, ParameterConfig::getValue));
//                    List<HasMetadata> processTemplate = processTemplate(templatePath, params);
//
//                    projectStates.add(new ProjectState(nsName, processTemplate));
//                }
//            } catch (Exception e) {
//                String appName = null;
//                if(app.getAppConfig() != null && app.getAppConfig().getName() != null) {
//                    appName = app.getAppConfig().getName();
//                }
//                LOG.error("Could not process app "+appName, e);
//            }
//        }
        return projectStates;
    }

    private Group lookupGroup(AppConfig appConfig, Map<String, Group> groups) {
        Objects.requireNonNull(appConfig);
        Objects.requireNonNull(groups);
        Group group = groups.get(appConfig.getGroup());
        if(group == null) {
            throw new IllegalStateException("Group '"+appConfig.getGroup()+"' configured in '"+appConfig.getName()+"' not found");
        }
        return group;
    }


}
