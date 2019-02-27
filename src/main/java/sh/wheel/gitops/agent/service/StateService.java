package sh.wheel.gitops.agent.service;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.model.NamespaceState;
import sh.wheel.gitops.agent.model.WheelRepository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

@Service
public class StateService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final WheelRepositoryService wheelRepositoryService;
    private final ConfigProcessingService configProcessingService;

    @Value("${sh.wheel.repository.url}")
    private String repositoryUrl;

    @Value("${sh.wheel.repository.branch}")
    private String repositoryBranch;

    private Map<String, NamespaceState> clusterState;
    private Map<String, NamespaceState> expectedState;

    @Autowired
    public StateService(WheelRepositoryService wheelRepositoryService, ConfigProcessingService configProcessingService) {
        this.wheelRepositoryService = wheelRepositoryService;
        this.configProcessingService = configProcessingService;
    }

    @PostConstruct
    public void init() throws IOException, GitAPIException {
        WheelRepository wheelRepository = wheelRepositoryService.loadRepository(repositoryUrl, repositoryBranch);
        List<NamespaceState> namespaceStates = configProcessingService.processExpectedNamespaceStates(wheelRepository);
//        gitRepositoryService.pullLatest();
//        WheelRepository repositoryState = repositoryConfigService.getRepositoryState(Paths.get(gitRepositoryService.getRepositoryPath()));


    }

    public Map<String, NamespaceState> getClusterState() {
        return clusterState;
    }

    public Map<String, NamespaceState> getExpectedState() {
        return expectedState;
    }
}
