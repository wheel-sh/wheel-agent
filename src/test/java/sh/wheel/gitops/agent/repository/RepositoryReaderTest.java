package sh.wheel.gitops.agent.repository;

import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.model.App;
import sh.wheel.gitops.agent.model.GitOpsRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RepositoryReaderTest {

    private String repositoryPath = this.getClass().getResource("/test-data1").getPath();

    @Test()
    void read_app_config() {
        RepositoryReader repositoryReader = new RepositoryReader();
        GitOpsRepository gitOpsRepository = repositoryReader.read(repositoryPath);
        List<App> apps = gitOpsRepository.getApps();
        assertEquals(1, apps.size());
        App app = apps.stream().findFirst().get();
        assertEquals("Demo", app.getName());
        assertEquals("example-group", app.getGroup());
    }
}