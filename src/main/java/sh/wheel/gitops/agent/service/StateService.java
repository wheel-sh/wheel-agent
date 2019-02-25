package sh.wheel.gitops.agent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.model.NamespaceState;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;

@Service
public class StateService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Map<String, NamespaceState> clusterState;
    private Map<String, NamespaceState> expectedState;

    @PostConstruct
    public void init() throws IOException {
        LOG.info("Loading state....");
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
