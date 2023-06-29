package pandas.gather;

import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;

public record FindAndReplaceForm(
        String directory,
        String filename,
        boolean recursive,
        String find,
        String replaceWith,
        boolean reportMode
) {
    public static final FindAndReplaceForm DEFAULTS = new FindAndReplaceForm(
            "", "*.htm*", true, "", "", true);

    public String encode(long instanceId, String email) {
        var builder = new StringBuilder();
        appendFormParam(builder, "instanceId", String.valueOf(instanceId));
        appendFormParam(builder, "email", email);
        appendFormParam(builder, "directory", directory);
        appendFormParam(builder, "filename", filename);
        appendFormParam(builder, "recursive", String.valueOf(recursive));
        appendFormParam(builder, "find", find);
        appendFormParam(builder, "replaceWith", replaceWith);
        appendFormParam(builder, "reportMode", String.valueOf(reportMode));
        return builder.toString();
    }

    private static void appendFormParam(StringBuilder builder, String key, String value) {
        if (value == null) return;
        if (builder.length() > 0) builder.append('&');
        builder.append(URLEncoder.encode(key, UTF_8));
        builder.append('=');
        builder.append(URLEncoder.encode(value, UTF_8));
    }
}
