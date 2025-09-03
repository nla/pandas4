package pandas.gather;

public enum BrowsertrixExit {
    SUCCESS(0),
    GENERIC_ERROR(1),
    OUT_OF_SPACE(3),
    FAILED(9),
    BROWSER_CRASHED(10),
    SIGNAL_INTERRUPTED(11),
    FAILED_LIMIT(12),
    SIGNAL_INTERUPTTED_FORCE(13),
    SIZE_LIMIT(14),
    TIME_LIMIT(15),
    PROXY_ERROR(21),
    UPLOAD_FAILED(22);

    private final int code;

    BrowsertrixExit(int code) {
        this.code = code;
    }

    public static Object forCode(int exitStatus) {
        for (var e : values()) {
            if (e.code == exitStatus) return e;
        }
        return null;
    }

    public int code() {
        return code;
    }
}
