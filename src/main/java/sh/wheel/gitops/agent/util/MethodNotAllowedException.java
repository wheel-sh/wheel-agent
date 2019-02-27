package sh.wheel.gitops.agent.util;

public class MethodNotAllowedException extends RuntimeException {
    public MethodNotAllowedException() {
    }

    public MethodNotAllowedException(String s) {
        super(s);
    }

    public MethodNotAllowedException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public MethodNotAllowedException(Throwable throwable) {
        super(throwable);
    }

    public MethodNotAllowedException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
