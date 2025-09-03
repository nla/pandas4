package pandas.gatherer.core;

public class GatherException extends Exception {
    private Integer exitStatus;

    public GatherException(String message, int exitStatus) {
        super(message);
        this.exitStatus = exitStatus;
    }

    public GatherException(String message, Throwable cause) {
        super(message, cause);
    }

    public Integer getExitStatus() {
        return exitStatus;
    }
}
