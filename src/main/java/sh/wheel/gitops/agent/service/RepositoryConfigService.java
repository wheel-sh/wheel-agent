package sh.wheel.gitops.agent.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sh.wheel.gitops.agent.config.AppConfig;
import sh.wheel.gitops.agent.config.BuildConfig;
import sh.wheel.gitops.agent.config.NamespaceConfig;
import sh.wheel.gitops.agent.model.App;
import sh.wheel.gitops.agent.model.WheelRepository;
import sh.wheel.gitops.agent.util.GenericYamlDeserializer;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RepositoryConfigService {

    public static final String APP_CONFIG = "config.yaml";
    public static final String BUILD_CONFIG_DIR = "build/";
    public static final String NAMESPACE_CONFIG_DIR = "namespace/";
    public static final String TEMPLATE_DIR = "template/";

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final GenericYamlDeserializer deserializer = new GenericYamlDeserializer();






    public List<App> readAllApps(Path appsDir) throws IOException {
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

    public App readApp(Path appDir) throws IOException {
        AppConfig appConfig = deserializer.deserialize(appDir.resolve(APP_CONFIG), AppConfig.class);
        List<BuildConfig> buildConfigs = deserializer.readDirectory(appDir.resolve(BUILD_CONFIG_DIR), BuildConfig.class);
        List<NamespaceConfig> namespaceConfigs = deserializer.readDirectory(appDir.resolve(NAMESPACE_CONFIG_DIR), NamespaceConfig.class);
        return new App(appConfig, buildConfigs, namespaceConfigs, appDir);
    }

    public WheelRepository getRepositoryState(Path repository) throws IOException {
        Path appsPath = repository.resolve("apps");
        List<App> apps = readAllApps(appsPath);
        return WheelRepository.newInstance(apps, new ArrayList<>());
    }

}
