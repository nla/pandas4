package pandas.render;

import org.attoparser.MarkupParser;
import org.attoparser.ParseException;
import org.attoparser.config.ParseConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PageInfoTest {

    @Test
    public void testTitleHandler() throws ParseException {
        PageInfo.TitleHandler handler = new PageInfo.TitleHandler();
        new MarkupParser(ParseConfiguration.htmlConfiguration()).parse("<html><head><title>test\n123</title></head><body><h1>h1", handler);
        assertEquals("test\n123", handler.title);
    }



}