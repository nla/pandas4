package pandas.core;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

/**
 * Helpers for formatting numbers for display.
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
}
