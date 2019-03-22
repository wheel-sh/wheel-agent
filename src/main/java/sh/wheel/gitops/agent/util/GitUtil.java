package sh.wheel.gitops.agent.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitUtil {

    public static void checkoutLatest(String gitUrl, String branch, Path target) throws GitAPIException, IOException {
        if (!Files.exists(target)) {
            Git.cloneRepository()
                    .setURI(gitUrl)
                    .setBranch(branch)
                    .setDirectory(target.toFile())
                    .call();
        } else {
            Git.open(target.toFile())
                    .pull()
                    .setRemoteBranchName(branch)
                    .setRebase(true)
                    .call();
        }
    }

}
