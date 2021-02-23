package pandas.gatherer.httrack;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HttrackUtilsTest {
    @Test
    public void parseExtra() {
        var extra = HttrackUtils.parseExtra("HTTP/1.1 200 OK\r\n" +
                "X-In-Cache: 1\r\n" +
                "X-StatusCode: 200\r\n" +
                "X-StatusMessage: OK\r\n" +
                "X-Size: 58455\r\n" +
                "Content-Type: text/html\r\n" +
                "X-Charset: utf-8\r\n" +
                "Last-Modified: Wed, 08 Jan 2014 04:26:16 GMT\r\n" +
                "X-Addr: example.com\r\n" +
                "X-Fil: /\r\n" +
                "X-Save: example.com/index.html\r\n");
        assertEquals("example.com/index.html", extra.get("X-Save"));
        assertEquals("58455", extra.get("X-Size"));
        assertEquals("1", extra.get("X-In-Cache"));
    }

    @Test
    public void mimeTypeByExtension() {
        assertEquals("application/smil", HttrackUtils.mimeTypeByExtension("foo.smi"));
        assertEquals("application/smil", HttrackUtils.mimeTypeByExtension("foo.bar.smil"));
        assertNull(HttrackUtils.mimeTypeByExtension("foo.bogus"));
    }
}