package sh.wheel.gitops.agent.testutil;

import sh.wheel.gitops.agent.service.OpenShiftService;
import sh.wheel.gitops.agent.util.MockOpenShiftRestClient;
import sh.wheel.gitops.agent.util.OpenShiftRestClient;
import sh.wheel.gitops.agent.util.OpenShiftTemplateUtil;

import java.nio.file.Path;

public class OpenShiftServiceTestUtil {

    public static OpenShiftService createWithMockData(Path mockData) {
        OpenShiftTemplateUtil templateUtil = OpenShiftTemplateUtil.create();
        OpenShiftRestClient openShiftRestClient = MockOpenShiftRestClient.createMockClient(mockData);
        OpenShiftService openShiftService = new OpenShiftService(templateUtil, openShiftRestClient);
        openShiftService.init();
        return openShiftService;
    }

}
