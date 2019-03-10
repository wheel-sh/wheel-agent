package sh.wheel.gitops.agent.service;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.model.ProjectState;
import sh.wheel.gitops.agent.model.WheelRepository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class StateService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final WheelRepositoryService wheelRepositoryService;
    private final ConfigProcessingService configProcessingService;
    @Value("${CONFIG_REPOSITORY_URL}")
    String repositoryUrl;
    @Value("${CONFIG_REPOSITORY_BRANCH:master}")
    String repositoryBranch;
    private OpenShiftService openShiftService;
    private List<ProjectState> processedProjectStates;
    private List<ProjectState> clusterProjectStates;


    @Autowired
    public StateService(WheelRepositoryService wheelRepositoryService, ConfigProcessingService configProcessingService, OpenShiftService openShiftService) {
        this.wheelRepositoryService = wheelRepositoryService;
        this.configProcessingService = configProcessingService;
        this.openShiftService = openShiftService;
    }

    @PostConstruct
    public void init() throws IOException, GitAPIException {
        WheelRepository wheelRepository = wheelRepositoryService.loadRepository(repositoryUrl, repositoryBranch);
        processedProjectStates = configProcessingService.processExpectedProjectStates(wheelRepository);
        clusterProjectStates = openShiftService.getProjectStatesFromCluster();


    }

    public List<ProjectState> getProcessedProjectStates() {
        return processedProjectStates;
    }

    public List<ProjectState> getClusterProjectStates() {
        return clusterProjectStates;
    }
}
