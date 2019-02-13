package sh.wheel.gitops.agent.repository;

import sh.wheel.gitops.agent.config.AppConfig;
import sh.wheel.gitops.agent.config.NamespaceConfig;
import sh.wheel.gitops.agent.config.TemplateConfig;
import sh.wheel.gitops.agent.model.App;
import sh.wheel.gitops.agent.model.BuildConfig;
import sh.wheel.gitops.agent.model.GitOpsRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RepositoryReader {

    private AppConfigDeserializer appConfigDeserializer = new AppConfigDeserializer();
    private BuildConfigDeserializer buildConfigDeserializer = new BuildConfigDeserializer();
    private NamespaceConfigDeserializer namespaceConfigDeserializer = new NamespaceConfigDeserializer();
    private TemplateConfigDeserializer templateConfigDeserializer = new TemplateConfigDeserializer();

    public GitOpsRepository read(String repositoryPath) throws IOException {
        Path appsPath = Paths.get(repositoryPath + "/apps");
        List<App> apps = getAppsConfigs(appsPath);
        return new GitOpsRepository(apps, null);
    }

    private List<App> getAppsConfigs(Path appsPath) throws IOException {
        try (Stream<Path> appPaths = Files.walk(appsPath)) {
            return appPaths.map(ap -> readAppConfigs(ap)).collect(Collectors.toList());
        }
    }

    public App readAppConfigs(Path appPath) {
        try {
            AppConfig appConfig = appConfigDeserializer.deserialize(Files.newInputStream(appPath.resolve("config.yaml")));
            List<BuildConfig> buildConfigPaths = buildConfigDeserializer.readDirectory(appPath.resolve("build"));
            List<NamespaceConfig> namespaceConfigPaths = namespaceConfigDeserializer.readDirectory(appPath.resolve("namespace"));
            List<TemplateConfig> templateConfigsPath = templateConfigDeserializer.readDirectory(appPath.resolve("template"));
            return new App(appConfig, buildConfigPaths, namespaceConfigPaths, templateConfigsPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
