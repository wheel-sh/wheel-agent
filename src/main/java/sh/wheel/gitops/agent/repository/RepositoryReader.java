package sh.wheel.gitops.agent.repository;

import sh.wheel.gitops.agent.config.AppConfig;
import sh.wheel.gitops.agent.model.App;
import sh.wheel.gitops.agent.model.GitOpsRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RepositoryReader {

    private AppConfigDeserializer appConfigDeserializer = new AppConfigDeserializer();

    public GitOpsRepository read(String repositoryPath) {
        File[] appDirs = new File(repositoryPath + "/apps").listFiles(File::isDirectory);
        if(appDirs == null) {
            throw new IllegalStateException("Directory /apps found");
        }
        List<App> apps = Arrays.stream(appDirs).map(this::readAppConfigs).collect(Collectors.toList());
        return new GitOpsRepository(null, apps);
    }

    public App readAppConfigs(File appDirectory) {
        File[] appConfigFile = appDirectory.listFiles((dir, name) -> "config.yaml".equals(name));
        if(appConfigFile == null || appConfigFile.length != 1) {
            throw new IllegalStateException("config.yaml not found in " + appDirectory.getPath());
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(appConfigFile[0]);
            AppConfig appConfig = appConfigDeserializer.deserialize(inputStream);

            return new App(appConfig.getName(), null, null, null, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
