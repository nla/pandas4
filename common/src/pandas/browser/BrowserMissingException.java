package pandas.browser;

import java.io.IOException;

public class BrowserMissingException extends IOException {
    public BrowserMissingException(String message) {
        super(message);
    }
}
