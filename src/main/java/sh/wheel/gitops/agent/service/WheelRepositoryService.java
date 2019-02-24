package sh.wheel.gitops.agent.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.model.WheelRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class WheelRepositoryService {

    @Value("#{java.nio.file.Paths.get('${sh.wheel.repository.base.path:/tmp/wheel-repos}')}")
    Path repositoryBasePath;

    public WheelRepository loadRepository(String gitURI, String branch) throws IOException, GitAPIException {
        Path localClone = cloneOrPullRepository(gitURI, branch);

        return null;
    }

    Path cloneOrPullRepository(String gitURI, String branch) throws IOException, GitAPIException {
        Files.createDirectories(repositoryBasePath);
        Path gitPath = Paths.get(gitURI);
        Path repoName = gitPath.getFileName();
        Path localRepositoryPath = repositoryBasePath.resolve(repoName);
        if (Files.exists(localRepositoryPath.resolve(".git"))) {
            pullRepository(branch, localRepositoryPath);
        } else {
            cloneRepository(gitURI, branch, localRepositoryPath);
        }
        return localRepositoryPath;
    }

    private void cloneRepository(String gitURI, String branch, Path localRepositoryPath) throws GitAPIException {
        Git.cloneRepository()
                .setURI(gitURI)
                .setRemote("origin")
                .setBranch(branch)
                .setDirectory(localRepositoryPath.toFile())
                .call();
    }

    private void pullRepository(String branch, Path localRepositoryPath) throws IOException, GitAPIException {
        Git git = Git.open(localRepositoryPath.toFile());
        PullResult pull = git
                .pull()
                .setRemote("origin")
                .setRemoteBranchName(branch)
                .setRebase(true)
                .call();
    }

}
