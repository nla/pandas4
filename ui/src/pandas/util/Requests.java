package pandas.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.springframework.http.HttpHeaders.REFERER;

public class Requests {

    public static String backlinkOrDefault(String defaultValue) {
        String backlink = backlink();
        return backlink == null ? defaultValue : backlink;
    }

    public static String backlink() {
        var request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
        String referer = request.getHeader(REFERER);
        if (referer != null) {
            String prefix = request.getRequestURL().toString().replaceFirst("(https?://[^/]+)/.*", "$1");
            prefix += request.getContextPath() + "/";
            if (referer.startsWith(prefix)) {
                return referer.substring(prefix.length() - 1);
            }
        }
        return null;
    }
}
