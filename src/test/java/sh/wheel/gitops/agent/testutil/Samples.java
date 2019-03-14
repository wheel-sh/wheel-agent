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
    PROJECT1("testrepo1/apps/example-app/env/test.yaml"),
    BASE_PROJECT_TEMPLATE("testrepo1/base/template/project.yaml"),
    EXAMPLE_APP_PROCESSED_DIR("json/example-app-test/processed/"),
    EXAMPLE_APP_PROCESSED_FILE("json/example-app-test/processed/example-app-processed.json"),
    EXAMPLE_APP_PROJECT_PROCESSED_FILE("json/example-app-test/processed/example-app-project-processed.json"),
    EXAMPLE_APP_SERVER_RESPONSE_DIR("json/example-app-test/server-response/"),
    EXAMPLE_APP_PROJECT_SERVER_RESPONSE("json/example-app-test/server-response/Project.json_"),
    EXAMPLE_MANAGEABLE_PROJECTS("hack/manageable-projects.json"),
    EXAMPLE_API_RESOURCES_WIDE("hack/api-resources-wide.txt"),
    EXAMPLE_PROCESSED_ROUTE("hack/Route.json"),
    TESTREPO1("testrepo1/");

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
