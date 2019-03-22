package sh.wheel.gitops.agent.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.config.*;
import sh.wheel.gitops.agent.model.App;
import sh.wheel.gitops.agent.model.BaseConfig;
import sh.wheel.gitops.agent.model.Group;
import sh.wheel.gitops.agent.model.WheelRepository;
import sh.wheel.gitops.agent.util.GenericYamlDeserializer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WheelRepositoryService {

    private static final String GROUP_CONFIG = "config.yaml";
    private static final String BUILD_CONFIG_DIR = "build/";
    private static final String PROJECT_CONFIG_DIR = "env/";
    private static final String APPS_DIR = "apps";
    private static final String GROUPS_DIR = "groups";
    private static final String BASE_DIR = "base";
    private static final String BASE_TEMPLATE_DIR = "template";
    private static final String BASE_PROJECT_TEMPLATE = "project.yaml";

    private static final String GROUP_MEMBERS_CONFIG = "members.yaml";

    private final GenericYamlDeserializer deserializer = new GenericYamlDeserializer();

    @Value("${CHECKOUT_BASE_PATH:/tmp/wheel-repos}")
    public Path repositoryBasePath;


    WheelRepository loadRepository(String gitURI, String branch) throws IOException, GitAPIException {
        Path localClone = cloneOrPullRepository(gitURI, branch);
        return getRepositoryState(localClone);
    }

    Path cloneOrPullRepository(String gitURI, String branch) throws IOException, GitAPIException {
        String repoName = getRepoName(gitURI);
        Path localRepositoryPath = repositoryBasePath.resolve(repoName);
        if (Files.exists(localRepositoryPath.resolve(".git"))) {
            pullRepository(branch, localRepositoryPath);
        } else {
            cloneRepository(gitURI, branch, localRepositoryPath);
        }
        return localRepositoryPath;
    }

    private String getRepoName(String gitURI) {
        String path = gitURI;
        if (gitURI.endsWith(".git")) {
            int gitIndex = gitURI.lastIndexOf(".git");
            path = gitURI.substring(0, gitIndex);
        }
        int slashIndex = gitURI.lastIndexOf("/");
        int backslahIndex = gitURI.lastIndexOf("\\");
        if (slashIndex > 0) {
            return path.substring(slashIndex + 1);
        } else if (backslahIndex > 0) {
            return path.substring(backslahIndex + 1);
        } else {
            throw new IllegalArgumentException("Cannot convert gitURI " + gitURI + " to repoName");
        }
    }

    private void cloneRepository(String gitURI, String branch, Path localRepositoryPath) throws GitAPIException {
        Git.cloneRepository()
                .setURI(gitURI)
                .setRemote("origin")
                .setBranch(branch)
                .setDirectory(localRepositoryPath.toFile())
                .call()
                .close();
    }

    private void pullRepository(String branch, Path localRepositoryPath) throws IOException, GitAPIException {
        Git git = Git.open(localRepositoryPath.toFile());
        git.pull()
                .setRemote("origin")
                .setRemoteBranchName(branch)
                .setRebase(true)
                .call();
        git.close();
    }


    List<App> readAllApps(Path appsDir) throws IOException {
        List<Path> appDirs = Files.list(appsDir).collect(Collectors.toList());
        List<App> apps = new ArrayList<>();
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
        List<BuildConfig> buildConfigs = new ArrayList<>(deserializer.readDirectory(appDir.resolve(BUILD_CONFIG_DIR), BuildConfig.class).values());
        Map<String, EnvConfig> envConfigs = deserializer.readDirectory(appDir.resolve(PROJECT_CONFIG_DIR), EnvConfig.class);
        return new App(appDir.getFileName().toString(), appConfig, buildConfigs, envConfigs, appDir);
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
        Path baseDir = repository.resolve(BASE_DIR);
        if (!Files.exists(appsDir)) {
            throw new FileNotFoundException(String.format("Could not find %s directory in %s", appsDir.toAbsolutePath().toString(), repository));
        }
        if (!Files.exists(groupsDir)) {
            throw new FileNotFoundException(String.format("Could not find %s directory in %s", groupsDir.toAbsolutePath().toString(), repository));
        }
        if (!Files.exists(baseDir)) {
            throw new FileNotFoundException(String.format("Could not find %s directory in %s", baseDir.toAbsolutePath().toString(), repository));
        }
        List<App> apps = readAllApps(appsDir);
        List<Group> groups = readAllGroups(groupsDir);
        BaseConfig baseConfig = readBaseConfig(baseDir);
        return WheelRepository.newInstance(apps, groups, baseConfig);
    }

    private BaseConfig readBaseConfig(Path baseDir) throws FileNotFoundException {
        Path projectTemplate = baseDir.resolve(BASE_TEMPLATE_DIR).resolve(BASE_PROJECT_TEMPLATE);
        if (!Files.exists(projectTemplate)) {
            throw new FileNotFoundException(String.format("Could not find %s directory in %s", projectTemplate.toAbsolutePath().toString(), baseDir.toAbsolutePath().toString()));
        }
        return new BaseConfig(projectTemplate);
    }

}
