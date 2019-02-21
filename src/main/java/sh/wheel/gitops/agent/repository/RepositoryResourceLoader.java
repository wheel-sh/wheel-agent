package sh.wheel.gitops.agent.repository;

import sh.wheel.gitops.agent.model.App;
import sh.wheel.gitops.agent.model.WheelRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RepositoryResourceLoader {

    public WheelRepository getRepositoryState(Path repository) throws IOException {
        Path appsPath = repository.resolve("apps");
        List<App> apps = new AppsConfigReader().readAllApps(appsPath);
        return WheelRepository.newInstance(apps, new ArrayList<>());
    }

}
