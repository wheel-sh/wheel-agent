package sh.wheel.gitops.agent.testutil;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

public enum Samples {


    TEMPLATE1("testrepo1/apps/example-app/template/app.v1.yaml"),
    NAMESPACE1("testrepo1/apps/example-app/namespace/test.yaml");

    private static final String SAMPLES_FOLDER = "/samples/";
    public static final String TESTREPO1_PATH = SAMPLES_FOLDER + "testrepo1/";
    public static final String TESTREPO2_PATH = SAMPLES_FOLDER + "testrepo2/";

    private final String filePath;

    Samples(String fileName) {
        this.filePath = SAMPLES_FOLDER + fileName;
    }

    Samples(String root, String filename) {
        this.filePath = root + filename;
    }

    public String getContentAsString() {
        String content = null;
        try {
            final InputStream contentStream = Samples.class.getResourceAsStream(filePath);
            content = IOUtils.toString(contentStream, "UTF-8");
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("Could not read file " + filePath + ": " + e.getMessage());
        }
        return content;
    }
}
