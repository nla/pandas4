package pandas.render;

import org.attoparser.ParseException;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class HtmlCharsetTest {
    private static String detect(String html) throws ParseException, IOException {
        return HtmlCharset.detect(new BufferedInputStream(new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void testDetect() throws ParseException, IOException {
        assertEquals("utf-8", detect("<html><head><meta charset=utf-8></head>"));
        assertEquals("utf-8", detect("<html><head><meta content=\"text/html; charset=utf-8\" http-equiv='Content-Type'></head>"));
        assertEquals(null, detect("<html><head><meta content=\"/\" http-equiv='Content-Type'></head>"));
        assertEquals(null, detect("\1\0garbage<<<<<<<<<<<<<<<<"));
    }
}