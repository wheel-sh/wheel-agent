package sh.wheel.gitops.agent.testutil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public enum Samples {

    TEMPLATE1("testrepo1/apps/example-app/template/app.v1.yaml"),
    NAMESPACE1("testrepo1/apps/example-app/namespace/test.yaml"),
    EXAMPLE_APP_PROCESSED("json/example-app-test/processed/"),
    EXAMPLE_APP_SERVER_RESPONSE("json/example-app-test/server-response/");

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
        try {
            InputStream resourceAsStream = Samples.class.getResourceAsStream(filePath);
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(resourceAsStream))) {
                return buffer.lines().collect(Collectors.joining("\n"));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("Could not read file " + filePath + ": " + e.getMessage());
        }
    }

    public Path toPath() {
        try {
            return Paths.get(Samples.class.getResource(filePath).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFilePath() {
        return filePath;
    }
}
