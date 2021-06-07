package pandas.util;

public class Strings {
    public static boolean isNullOrBlank(String s) {
        return s == null || s.isBlank();
    }

    public static String emptyToNull(String s) {
        return s != null && s.isEmpty() ? null : s;
    }
}
