package sh.wheel.gitops.agent.testutil;

import com.openshift.restclient.ClientBuilder;
import com.openshift.restclient.IClient;
import com.openshift.restclient.model.kubeclient.ICluster;
import com.openshift.restclient.model.kubeclient.IKubeClientConfig;
import com.openshift.restclient.model.kubeclient.IUser;
import com.openshift.restclient.model.kubeclient.KubeClientConfigSerializer;

import javax.el.PropertyNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHelper {

    public static final int DEFAULT_PROXY_PORT = 3128;

    public IClient createClient() {
        String openshiftUrl = System.getProperty("openshift.url");
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        if(openshiftUrl == null || openshiftUrl.isEmpty()) {
            throw new PropertyNotFoundException("No property openshift.url provided. Please start with -Dopenshift.url=https://console.example.org:443");
        }
        ClientBuilder clientBuilder = new ClientBuilder(openshiftUrl);

        if(proxyHost != null || !proxyHost.isEmpty()) {
            int port = DEFAULT_PROXY_PORT;
            if(proxyPort != null && !proxyPort.isEmpty()) {
                port = Integer.valueOf(proxyPort);
            }
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, port));
            clientBuilder.proxy(proxy);
        }

        IUser user = loadUserWithTokenFromKubeconfig(openshiftUrl);
        IClient client = clientBuilder.withUserName(user.getName()).build();
        client.getAuthorizationContext().setToken(user.getToken());
        return client;
    }

    private IUser loadUserWithTokenFromKubeconfig(String clusterName) {
        try {
            String userHome = System.getProperty("user.home");
            Path kubeConfigPath = Paths.get(userHome + "/.kube/config");
            byte[] bytes = new byte[0];
            String config = new String(Files.readAllBytes(kubeConfigPath));
            StringReader configReader = new StringReader(config);
            KubeClientConfigSerializer serializer = new KubeClientConfigSerializer();
            IKubeClientConfig iKubeClientConfig = serializer.loadKubeClientConfig(configReader);
            ICluster iCluster = iKubeClientConfig.getClusters().stream().filter(c -> c.getServer().contains(clusterName)).findFirst().get();
            String name = iCluster.getName();
            return iKubeClientConfig.getUsers().stream().filter(u -> u.getName().endsWith(name)).findFirst().get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
