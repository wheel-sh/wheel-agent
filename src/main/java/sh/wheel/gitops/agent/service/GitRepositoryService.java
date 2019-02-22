package sh.wheel.gitops.agent.service;


import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.util.GitUtil;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;

@Service
public class GitRepositoryService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${sh.wheel.repository.url:}")
    private String repositoryUrl;

    @Value("${sh.wheel.repository.branch:}")
    private String repositoryBranch;

    @Value("${sh.wheel.repository.target:/tmp/repository}")
    private String repositoryPath;


    public void pullLatest() {
        try {
            GitUtil.checkoutLatest(repositoryUrl, repositoryBranch, Paths.get(repositoryPath));
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public String getRepositoryBranch() {
        return repositoryBranch;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }
}
