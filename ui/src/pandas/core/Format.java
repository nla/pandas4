package pandas.core;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.net.URI;

/**
 * Helpers for formatting numbers and URLs for display.
 */
@Service
public class Format {
    public String comma(long number) {
        return String.format("%,d", number);
    }

    public String bytes(long x) {
        return FileUtils.byteCountToDisplaySize(x);
    }

    public String statusClass(Integer status) {
        if (status == null || status < 100) {
            return "status-unknown";
        } else if (status < 300) {
            return "status-2xx";
        } else if (status < 400) {
            return "status-3xx";
        } else if (status < 500) {
            return "status-4xx";
        } else if (status < 600) {
            return "status-5xx";
        } else {
            return "status-unknown";
        }
    }

    /**
     * Returns the site part of a URL. Removing the protocol, www. prefix and path.
     */
    public String site(String url) {
        if (url == null) return null;
        String host;
        try {
            host = URI.create(url).getHost();
        } catch (IllegalArgumentException ignored) {
            return url;
        }
        if (host.startsWith("www.")) host = host.substring(4);
        return host;
    }
}
