package sh.wheel.gitops.agent.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.config.*;
import sh.wheel.gitops.agent.model.App;
import sh.wheel.gitops.agent.model.Group;
import sh.wheel.gitops.agent.model.WheelRepository;
import sh.wheel.gitops.agent.util.GenericYamlDeserializer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WheelRepositoryService {

    public static final String APP_CONFIG = "config.yaml";
    public static final String GROUP_CONFIG = "config.yaml";
    public static final String BUILD_CONFIG_DIR = "build/";
    public static final String NAMESPACE_CONFIG_DIR = "namespace/";
    public static final String TEMPLATE_DIR = "template/";
    public static final String APPS_DIR = "apps";
    public static final String GROUPS_DIR = "groups";
    public static final String GROUP_MEMBERS_CONFIG = "members.yaml";

    private final GenericYamlDeserializer deserializer = new GenericYamlDeserializer();

    @Value("#{java.nio.file.Paths.get('${sh.wheel.repository.base.path:/tmp/wheel-repos}')}")
    Path repositoryBasePath;


    public WheelRepository loadRepository(String gitURI, String branch) throws IOException, GitAPIException {
        Path localClone = cloneOrPullRepository(gitURI, branch);
        return getRepositoryState(localClone);
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


    List<App> readAllApps(Path appsDir) throws IOException {
        List<Path> appDirs = Files.list(appsDir).collect(Collectors.toList());
        ArrayList<App> apps = new ArrayList<>();
        for (Path appDir : appDirs) {
            try {
                App app = readApp(appDir);
                apps.add(app);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return apps;
    }

    private App readApp(Path appDir) throws IOException {
        AppConfig appConfig = deserializer.deserialize(appDir.resolve(GROUP_CONFIG), AppConfig.class);
        List<BuildConfig> buildConfigs = deserializer.readDirectory(appDir.resolve(BUILD_CONFIG_DIR), BuildConfig.class);
        List<NamespaceConfig> namespaceConfigs = deserializer.readDirectory(appDir.resolve(NAMESPACE_CONFIG_DIR), NamespaceConfig.class);
        return new App(appConfig, buildConfigs, namespaceConfigs, appDir);
    }


    List<Group> readAllGroups(Path groupsDir) throws IOException {
        List<Path> groupDirs = Files.list(groupsDir).collect(Collectors.toList());
        ArrayList<Group> groups = new ArrayList<>();
        for (Path groupDir : groupDirs) {
            try {
                Group group = readGroup(groupDir);
                groups.add(group);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return groups;
    }

    private Group readGroup(Path groupDir) throws IOException {
        GroupConfig groupConfig = deserializer.deserialize(groupDir.resolve(GROUP_CONFIG), GroupConfig.class);
        MembersConfig membersConfig = deserializer.deserialize(groupDir.resolve(GROUP_MEMBERS_CONFIG), MembersConfig.class);
        return new Group(groupConfig, membersConfig);
    }

    WheelRepository getRepositoryState(Path repository) throws IOException {
        Path appsDir = repository.resolve(APPS_DIR);
        Path groupsDir = repository.resolve(GROUPS_DIR);
        if (!Files.exists(appsDir)) {
            throw new FileNotFoundException("Could not find apps/ directory in " + repository);
        }
        if (!Files.exists(groupsDir)) {
            throw new FileNotFoundException("Could not find groups/ directory in " + repository);
        }
        List<App> apps = readAllApps(appsDir);
        List<Group> groups = readAllGroups(groupsDir);
        return WheelRepository.newInstance(apps, groups);
    }

}
