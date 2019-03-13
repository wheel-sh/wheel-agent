package sh.wheel.gitops.agent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.model.*;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AgentService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final StateService stateService;
    private final ProjectDifferenceService projectDifferenceService;
    private final ResourceDifferenceService resourceDifferenceService;
    private final OpenShiftService openShiftService;

    public AgentService(StateService stateService, ProjectDifferenceService projectDifferenceService, ResourceDifferenceService resourceDifferenceService, OpenShiftService openShiftService) {
        this.stateService = stateService;
        this.projectDifferenceService = projectDifferenceService;
        this.resourceDifferenceService = resourceDifferenceService;
        this.openShiftService = openShiftService;
    }

    public void synchronize() {
        Map<String, ProjectState> clusterStateByProject = stateService.getClusterProjectStates().stream().collect(Collectors.toMap(ProjectState::getName, Function.identity()));
        Map<String, ProjectState> processedStateByProject = stateService.getProcessedProjectStates().stream().collect(Collectors.toMap(ProjectState::getName, Function.identity()));
        Set<String> projectNames = Stream.concat(clusterStateByProject.keySet().stream(), processedStateByProject.keySet().stream()).collect(Collectors.toSet());
        for (String projectName : projectNames) {
            ProjectState clusterState = clusterStateByProject.get(projectName);
            ProjectState processedState = processedStateByProject.get(projectName);
            if (clusterState == null) {
                createProject(processedState);
            } else if (processedState == null) {
                deleteProject(clusterState.getProject());
            } else {
                List<ResourceDifference> resourceDifferences = projectDifferenceService.evaluateDifference(processedState, clusterState);
                List<ResourceAction> resourceActions = resourceDifferenceService.createResourceActions(resourceDifferences, processedState, clusterState);
                execute(projectName, resourceActions);
            }
        }

    }

    private void execute(String projectName, List<ResourceAction> resourceActions) {
        for (ResourceAction resourceAction : resourceActions) {
            try {
                switch (resourceAction.getType()) {
                    case CREATE:
                        openShiftService.createNamespacedResource(resourceAction.getResource(), projectName);
                        break;
                    case PATCH:
                        openShiftService.patch(resourceAction.getResource(), resourceAction.getAttributeDifferences(), projectName);
                        break;
                    case DELETE:
                        openShiftService.delete(resourceAction.getResource());
                        break;
                    case WARNING:
                        LOG.warn(String.format("Project %s has warning in diff %s", projectName, resourceAction));
                        break;
                    case IGNORE:
                        break;
                }
            } catch (Exception e) {
                LOG.warn("Error while executing resource action: "+resourceAction, e);
            }
        }
    }

    private void deleteProject(Resource project) {
        openShiftService.delete(project);
    }

    private void createProject(ProjectState repositoryState) {
        Map<ResourceKey, Resource> resourcesByKey = repositoryState.getResourcesByKey();
        String projectName = repositoryState.getProject().getName();
        openShiftService.newProject(projectName);

        Set<ResourceKey> resources = resourcesByKey.keySet();
        resources.remove(ResourceKey.projectWithName(projectName));
        List<ResourceKey> roleBinding = resources.stream().filter(rk -> rk.getKind().equals("RoleBinding")).collect(Collectors.toList());
        resources.removeAll(roleBinding);
        resources.forEach(r -> openShiftService.createNamespacedResource(resourcesByKey.get(r), projectName));
    }
}
