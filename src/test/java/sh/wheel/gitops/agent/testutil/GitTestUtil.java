package sh.wheel.gitops.agent.testutil;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitTestUtil {

    public static void initGitRepoIfNotExists(Path path) throws GitAPIException {
        if (!Files.exists(path.resolve(".git"))) {
            Git git = Git.init()
                    .setDirectory(path.toFile())
                    .call();
            git.add()
                    .addFilepattern(".")
                    .call();
            git.commit()
                    .setMessage("Initial commit")
                    .call();
        }
    }

    public static String getHeadId(Path repository) {
        try {
            return Git.open(repository.toFile()).getRepository().resolve(Constants.HEAD).getName();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void addEmptyCommit(Path repository, String message) {
        try {
            Git.open(repository.toFile()).commit().setMessage(message).call();
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
