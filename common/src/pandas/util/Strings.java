package pandas.util;

public class Strings {
    public static boolean isNullOrBlank(String s) {
        return s == null || s.isBlank();
    }

    public static String emptyToNull(String s) {
        return s != null && s.isEmpty() ? null : s;
    }

    public static String removePrefix(String prefix, String str) {
        return str.startsWith(prefix) ? str.substring(prefix.length()) : str;
    }
}
