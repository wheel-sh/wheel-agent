package sh.wheel.gitops.agent.repository;

import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.config.NamespaceConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NamespaceConfigDeserializerTest {

    @Test
    void deserialize() throws FileNotFoundException {
        String path = this.getClass().getResource("/test-data1/apps/example-app/namespace/test.yaml").getPath();
        InputStream inputStream = new FileInputStream(path);

        NamespaceConfig deserialize = new NamespaceConfigDeserializer().deserialize(inputStream);

        assertEquals("example-app-test", deserialize.getName());
        assertEquals(3, deserialize.getParameters().size());
        assertNotNull(deserialize.getLimits());
        assertNotNull(deserialize.getRequests());
        assertNotNull(deserialize.getTemplate());
        assertEquals("example-app", deserialize.getTemplate().getName());
    }
}