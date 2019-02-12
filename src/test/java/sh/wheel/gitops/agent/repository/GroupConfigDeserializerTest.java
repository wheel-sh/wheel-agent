package sh.wheel.gitops.agent.repository;

import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.config.GroupConfig;
import sh.wheel.gitops.agent.config.PoolConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupConfigDeserializerTest {

    @Test
    void deserialize() throws FileNotFoundException {
        String path = this.getClass().getResource("/test-data1/groups/example-group/config.yaml").getPath();
        InputStream inputStream = new FileInputStream(path);

        GroupConfig deserialize = new GroupConfigDeserializer().deserialize(inputStream);

        assertEquals("example-group", deserialize.getName());
        assertEquals(2, deserialize.getPools().size());
        PoolConfig poolConfig = deserialize.getPools().stream().findFirst().get();
        assertEquals("production", poolConfig.getName());
        assertEquals("10Gi", poolConfig.getLimits().getMemory());
    }
}