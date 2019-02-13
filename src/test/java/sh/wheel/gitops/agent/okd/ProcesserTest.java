package sh.wheel.gitops.agent.okd;

import com.openshift.restclient.ClientBuilder;
import com.openshift.restclient.IClient;
import org.junit.jupiter.api.Test;

class ProcesserTest {

//    oc login https://openshift.nikio.io:443 --token=a8Yc9mbuK3VdbFpofjiMTuGAAcKccGrOd34wyIYC34s

    @Test
    void process() {
        IClient client = new ClientBuilder("https://openshift.nikio.io")
                .withUserName("admin@nikio.io")
                .build();
        client.getAuthorizationContext().setToken("a8Yc9mbuK3VdbFpofjiMTuGAAcKccGrOd34wyIYC34s");

        System.out.println();
    }
}