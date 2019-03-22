package sh.wheel.gitops.agent.model;

import java.util.Arrays;

public enum Operation {

    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    MOVE("move"),
    COPY("copy"),
    TEST("test");

    private final String name;


    Operation(String name) {
        this.name = name;
    }

    public static Operation byName(String name) {
        return Arrays.stream(values()).filter(v -> v.getName().toLowerCase().equals(name.toLowerCase())).findFirst().orElse(null);
    }

    private String getName() {
        return this.name;
    }
}
