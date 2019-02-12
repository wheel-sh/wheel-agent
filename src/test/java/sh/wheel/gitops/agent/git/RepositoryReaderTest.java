package sh.wheel.gitops.agent.git;

import org.wildfly.common.Assert;
import sh.wheel.gitops.agent.model.App;
import sh.wheel.gitops.agent.model.Repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryReaderTest {

    @org.junit.jupiter.api.Test
    void read() {
        RepositoryReader repositoryReader = new RepositoryReader();

        Repository repository = repositoryReader.read("/test-data1");
        List<App> apps = repository.getApps();

        Assert.assertTrue(apps.size() == 1);
        App app = apps.stream().findFirst().get();
        Assert.assertTrue(app.getName().equals("example-app"));
    }
}