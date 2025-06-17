package pandas.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Strings {
    public static boolean isNullOrBlank(String s) {
        return s == null || s.isBlank();
    }

    public static String emptyToNull(String s) {
        return s != null && s.isEmpty() ? null : s;
    }

    public static String clean(String s) {
        if (s == null || s.isEmpty()) return null;
        return s.trim();
    }

    public static String removePrefix(String prefix, String str) {
        return str.startsWith(prefix) ? str.substring(prefix.length()) : str;
    }

    public static String shellEncode(List<String> command) {
        return command.stream().map(s -> s.matches("[a-zA-Z0-9_.=@/:-]*") ?
                        s : "'" + s.replace("'", "'\\''") + "'")
                .collect(Collectors.joining(" "));
    }

    private static final Pattern SHELL_TOKEN_PATTERN = Pattern.compile("\"([^\"]*)\"|'([^']*)'|([^\\s]+)");

    public static List<String> shellSplit(String command) {
        var tokens = new ArrayList<String>();
        var matcher = SHELL_TOKEN_PATTERN.matcher(command);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(matcher.group(1));
            } else if (matcher.group(2) != null) {
                tokens.add(matcher.group(2));
            } else if (matcher.group(3) != null) {
                tokens.add(matcher.group(3));
            }
        }
        return tokens;
    }
}
