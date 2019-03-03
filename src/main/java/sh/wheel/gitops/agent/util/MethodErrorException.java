package sh.wheel.gitops.agent.util;

public class MethodErrorException extends RuntimeException {
    public MethodErrorException() {
    }

    public MethodErrorException(String s) {
        super(s);
    }

    public MethodErrorException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public MethodErrorException(Throwable throwable) {
        super(throwable);
    }

    public MethodErrorException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
