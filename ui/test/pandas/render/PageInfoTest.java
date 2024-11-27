package pandas.render;

import org.attoparser.MarkupParser;
import org.attoparser.ParseException;
import org.attoparser.config.ParseConfiguration;
import org.jsoup.Jsoup;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PageInfoTest {

    @Test
    public void testTitleHandler() throws ParseException {
        String html = "<html><head><title>   \t\ttest\n123\t\t\n  456</title></head><body><h1>h1";

        var pageInfo = new PageInfo(200, "OK", "text/html", Jsoup.parse(html));
        assertEquals("test 123 456", pageInfo.getTitle());
    }



}