package pandas.util;

public class Strings {
    public static String emptyToNull(String s) {
        return s != null && s.isEmpty() ? null : s;
    }
}
