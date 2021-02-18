package pandas.gatherer.heritrix;

import java.io.IOException;
import java.net.URI;

class HeritrixApiException extends IOException {

    HeritrixApiException(int statusCode, String reasonPhrase, URI uri) {
        super("" + statusCode + " " + reasonPhrase + " at " + uri);
    }

    HeritrixApiException(String message, Throwable e) {
        super(message, e);
    }
}
