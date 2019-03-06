package sh.wheel.gitops.agent.testutil;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;

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
            git.close();
        }
    }

    public static String getHeadId(Path repository) {
        try {
            Git open = Git.open(repository.toFile());
            Repository repository1 = open.getRepository();
            open.close();
            return repository1.resolve(Constants.HEAD).getName();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void addEmptyCommit(Path repository, String message) {
        try {
            Git open = Git.open(repository.toFile());
            open.commit().setMessage(message).call();
            open.close();
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
