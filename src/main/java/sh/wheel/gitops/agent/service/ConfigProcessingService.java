package sh.wheel.gitops.agent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.config.AppConfig;
import sh.wheel.gitops.agent.config.EnvConfig;
import sh.wheel.gitops.agent.config.ParameterConfig;
import sh.wheel.gitops.agent.model.App;
import sh.wheel.gitops.agent.model.Group;
import sh.wheel.gitops.agent.model.ProjectState;
import sh.wheel.gitops.agent.model.WheelRepository;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ConfigProcessingService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private OpenShiftService openShiftService;

    @Autowired
    public ConfigProcessingService(OpenShiftService openShiftService) {
        this.openShiftService = openShiftService;
    }

    public List<ProjectState> processExpectedProjectStates(WheelRepository repository) {
        List<ProjectState> projectStates = new ArrayList<>();
        Path projectTemplate = repository.getBaseConfig().getProjectTemplate();
        String whoAmI = openShiftService.getWhoAmI();
        for (Map.Entry<String, App> appEntrySet : repository.getApps().entrySet()) {
            String appName = appEntrySet.getKey();
            App app = appEntrySet.getValue();
            Group group = lookupGroup(app, repository.getGroups());
            for (Map.Entry<String, EnvConfig> envEntrySet : app.getEnvConfigs().entrySet()) {
                String envName = envEntrySet.getKey();
                EnvConfig envConfig = envEntrySet.getValue();
                String namespaceName = appName + "-" + envName;
                try {
                    Path appTemplate = app.getAppDir().resolve("template").resolve(envConfig.getTemplateFile());
                    if (!Files.exists(appTemplate)) {
                        throw new IllegalStateException(String.format("Cannot find template %s for app %s in project %s", appTemplate.toString(), app.getName(), namespaceName));
                    }
                    Map<String, String> appParams = envConfig.getParameters().stream().collect(Collectors.toMap(ParameterConfig::getName, ParameterConfig::getValue));
                    Map<String, String> projectParams = getProjectParams(namespaceName, whoAmI);
                    ProjectState processedProjectState = openShiftService.getProjectStateFromTemplate(projectTemplate, projectParams, appTemplate, appParams);
                    projectStates.add(processedProjectState);
                } catch (Exception e) {
                    LOG.error("Could not process app for namespace " + namespaceName, e);
                }

            }
        }
        return projectStates;
    }

    private Group lookupGroup(App appConfig, Map<String, Group> groups) {
        Objects.requireNonNull(appConfig);
        Objects.requireNonNull(groups);
        Group group = groups.get(appConfig.getAppConfig().getGroup());
        if (group == null) {
            throw new IllegalStateException("Group '" + appConfig.getAppConfig().getGroup() + "' configured in '" + appConfig.getName() + "' not found");
        }
        return group;
    }

    private Map<String, String> getProjectParams(String projectName, String whoAmI) {
        Map<String, String> params = new HashMap<>();
        params.put("PROJECT_NAME", projectName);
        params.put("PROJECT_REQUESTING_USER", whoAmI);
        params.put("PROJECT_ADMIN_USER", whoAmI);
        return params;
    }


}
