package sh.wheel.gitops.agent.util;

import org.junit.jupiter.api.Test;
import sh.wheel.gitops.agent.model.Resource;
import sh.wheel.gitops.agent.testutil.Samples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenShiftTemplateUtilTest {

    @Test
    void process() {
        Map<String, String> params = new HashMap<>();
        params.put("PROJECT_NAME", "example-app-test");
        params.put("PROJECT_DISPLAYNAME", "");
        params.put("PROJECT_DESCRIPTION", "");
        params.put("PROJECT_REQUESTING_USER", "demo@nikio.io");
        params.put("PROJECT_ADMIN_USER", "demo@nikio.io");

        OpenShiftTemplateUtil tplUtil = OpenShiftTemplateUtil.create();
        List<Resource> processedResources = tplUtil.process(Samples.BASE_PROJECT_TEMPLATE.toPath(), params);

        assertNotNull(processedResources);
        assertEquals(5, processedResources.size());
    }
}