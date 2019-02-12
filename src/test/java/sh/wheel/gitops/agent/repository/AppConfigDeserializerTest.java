package sh.wheel.gitops.agent.repository;

import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.config.AppConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppConfigDeserializerTest {

    @Test
    void deserialize() throws FileNotFoundException {
        String path = this.getClass().getResource("/test-data1/apps/example-app/config.yaml").getPath();
        InputStream inputStream = new FileInputStream(path);

        AppConfig deserialize = new AppConfigDeserializer().deserialize(inputStream);

        assertEquals("example-app", deserialize.getName());
        assertEquals("example-group", deserialize.getGroup());
        assertEquals("example-info", deserialize.getMetadata().get("custom"));
    }
}