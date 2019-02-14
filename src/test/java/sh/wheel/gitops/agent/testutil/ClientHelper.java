package sh.wheel.gitops.agent.testutil;

import com.openshift.restclient.ClientBuilder;
import com.openshift.restclient.IClient;
import com.openshift.restclient.model.kubeclient.IKubeClientConfig;
import com.openshift.restclient.model.kubeclient.IUser;
import com.openshift.restclient.model.kubeclient.KubeClientConfigSerializer;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHelper {

    public IClient createClient() {
        IUser user = loadUserWithTokenFromKubeconfig();
        IClient client = new ClientBuilder("")
                .withUserName(user.getName())
                .build();
        client.getAuthorizationContext().setToken(user.getToken());
        return client;
    }

    private IUser loadUserWithTokenFromKubeconfig() {
        try {
            String userHome = System.getProperty("user.home");
            Path kubeConfigPath = Paths.get(userHome + "/.kube/config");
            byte[] bytes = new byte[0];
            String config = new String(Files.readAllBytes(kubeConfigPath));
            StringReader configReader = new StringReader(config);
            KubeClientConfigSerializer serializer = new KubeClientConfigSerializer();
            IKubeClientConfig iKubeClientConfig = serializer.loadKubeClientConfig(configReader);
            return iKubeClientConfig.getUsers().stream().filter(u -> u.getToken() != null).findFirst().get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
