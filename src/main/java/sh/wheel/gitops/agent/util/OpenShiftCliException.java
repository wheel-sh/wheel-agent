package sh.wheel.gitops.agent.util;

public class OpenShiftCliException extends RuntimeException {

    public OpenShiftCliException() {
    }

    public OpenShiftCliException(String message) {
        super(message);
    }

    public OpenShiftCliException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenShiftCliException(Throwable cause) {
        super(cause);
    }

    public OpenShiftCliException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
