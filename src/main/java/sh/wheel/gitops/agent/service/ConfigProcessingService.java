package sh.wheel.gitops.agent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.config.AppConfig;
import sh.wheel.gitops.agent.config.ParameterConfig;
import sh.wheel.gitops.agent.config.ProjectConfig;
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
        for (App app : repository.getApps().values()) {
            try {
                Group group = lookupGroup(app.getAppConfig(), repository.getGroups());
                for (ProjectConfig projectConfig : app.getProjectConfigs()) {
                    Path appTemplate = app.getAppDir().resolve("template").resolve(projectConfig.getTemplateFile());
                    String projectName = projectConfig.getName();
                    if (!Files.exists(appTemplate)) {
                        throw new IllegalStateException(String.format("Cannot find template %s for app %s in project %s", appTemplate.toString(), app.getAppConfig().getName(), projectName));
                    }
                    Map<String, String> appParams = projectConfig.getParameters().stream().collect(Collectors.toMap(ParameterConfig::getName, ParameterConfig::getValue));
                    Map<String, String> projectParams = getProjectParams(projectName, whoAmI);
                    ProjectState processedProjectState = openShiftService.getProjectStateFromTemplate(projectTemplate, projectParams, appTemplate, appParams);
                    projectStates.add(processedProjectState);
                }
            } catch (Exception e) {
                String appName = null;
                if (app.getAppConfig() != null && app.getAppConfig().getName() != null) {
                    appName = app.getAppConfig().getName();
                }
                LOG.error("Could not process app " + appName, e);
            }
        }
        return projectStates;
    }

    private Group lookupGroup(AppConfig appConfig, Map<String, Group> groups) {
        Objects.requireNonNull(appConfig);
        Objects.requireNonNull(groups);
        Group group = groups.get(appConfig.getGroup());
        if (group == null) {
            throw new IllegalStateException("Group '" + appConfig.getGroup() + "' configured in '" + appConfig.getName() + "' not found");
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
