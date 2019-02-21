package sh.wheel.gitops.agent.model;

public enum Operation {

    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    MOVE("move"),
    COPY("copy"),
    TEST("test");

    private String name;


    private Operation(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
