package sh.wheel.gitops.agent.testutil;

import com.openshift.restclient.IClient;
import com.openshift.restclient.model.IList;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
class ClientHelperTest {

    @Test
    void create() {
        IClient client = new ClientHelper().createClient();
        IList project = client.get("project", null);
        assertNotNull(project);
    }
}