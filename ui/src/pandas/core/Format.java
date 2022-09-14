package pandas.core;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

@Service
public class Format {
    public String comma(long number) {
        return String.format("%,d", number);
    }

    public String bytes(long x) {
        return FileUtils.byteCountToDisplaySize(x);
    }
}
