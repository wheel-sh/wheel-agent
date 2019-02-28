package sh.wheel.gitops.agent.testutil;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import sh.wheel.gitops.agent.util.OpenShiftCli;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

public class OpenShiftCliMockUtil {

    private static final List<JsonNode> EXAMPLE_TEST_APP_RESOURCES_SERVER_RESPONSE = ProjectStateUtil.createExampleTestAppResourcesServerResponse();
    private static final JsonNode EXAMPLE_TEST_APP_RESOURCES_PROCESSED = ProjectStateUtil.createExampleTestAppResourcesProcessed();
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    public static OpenShiftCli createOpenShiftCliMock() {
        OpenShiftCli mock = Mockito.mock(OpenShiftCli.class);
        JsonNode exampleTestAppResourcesProcessed = deepCopy(EXAMPLE_TEST_APP_RESOURCES_PROCESSED);
        List<JsonNode> exampleTestAppResourcesServerResponse = EXAMPLE_TEST_APP_RESOURCES_SERVER_RESPONSE.stream().map(OpenShiftCliMockUtil::deepCopy).collect(Collectors.toList());
        Mockito.doReturn(exampleTestAppResourcesProcessed).when(mock).process(ArgumentMatchers.notNull(), ArgumentMatchers.notNull());
        Mockito.doReturn(exampleTestAppResourcesServerResponse).when(mock).getAllNamespacedResource(ArgumentMatchers.notNull());
        return mock;
    }

    private static JsonNode deepCopy(JsonNode jsonNode) {
        try {
            TokenBuffer tb = new TokenBuffer(OBJECT_MAPPER, false);
            OBJECT_MAPPER.writeValue(tb, jsonNode);
            return OBJECT_MAPPER.readTree(tb.asParser());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
