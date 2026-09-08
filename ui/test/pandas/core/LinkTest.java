package pandas.core;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import pandas.collection.Collection;
import pandas.collection.Title;
import pandas.gather.Instance;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LinkTest {
    private final LinkProperties properties = new LinkProperties(new Config(),
            "https://delivery.example/", "https://collections.example/", "https://titles.example/",
            "https://webdav.example/", "ftp://ftp.example/");

    @Test
    void internalLinksIncludeServletContextPath() {
        Link link = new Link(properties, "/pandas", "https://admin.example/pandas");

        assertEquals("/pandas/instances/42", link.toInstance(42));
        assertEquals("/pandas/subjects/7/icon", link.subjectIcon(7));
        assertEquals("https://admin.example/pandas/login/check-session-reply", link.checkSessionReply());
    }

    @Test
    void internalLinksAtRootDoNotHaveAnExtraSlash() {
        Link link = new Link(properties, "/", "https://admin.example/");

        assertEquals("/instances/42", link.toInstance(42));
        assertEquals("https://admin.example/login/check-session-reply", link.checkSessionReply());
    }

    @Test
    void configuredAndBambooLinksArePreserved() {
        Config config = new Config();
        config.setBambooUrl("https://bamboo.example/base/");
        LinkProperties properties = new LinkProperties(config,
                "https://delivery.example/", "https://collections.example/", "https://titles.example/",
                "https://webdav.example/", "ftp://ftp.example/");
        Link link = new Link(properties, "/pandas", "https://admin.example/pandas");

        assertEquals("https://delivery.example/20200102030405/https://seed.example/",
                link.delivery(Instant.parse("2020-01-02T03:04:05Z"), "https://seed.example/"));
        assertEquals("https://bamboo.example/base/crawls/99", link.toBambooCrawl(99));
        assertEquals("https://bamboo.example/base/warcs/a%20file.warc.gz/details",
                link.toBambooWarc("a file.warc.gz"));
    }

    @Test
    void configuredIdentifierWebdavAndFtpLinksArePreserved() {
        Link link = new Link(properties, "/pandas", "https://admin.example/pandas");
        Collection collection = new Collection();
        collection.setId(12L);
        Title title = new Title();
        title.setPi(34L);
        Instance instance = new Instance(title, Instant.parse("2020-01-02T03:04:00Z"), "test");

        assertEquals("https://collections.example/12", link.identifier(collection));
        assertEquals("https://titles.example/34", link.identifier(title));
        assertEquals("https://webdav.example/34/" + instance.getDateString(), link.webdav(instance));
        assertEquals("ftp://ftp.example/34/" + instance.getDateString(), link.ftp(instance));
    }

    @Test
    void adviceBuildsLinkFromTheCurrentRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("admin.example");
        request.setServerPort(443);
        request.setContextPath("/pandas");

        Link link = new LinkControllerAdvice(properties).link(request);

        assertEquals("/pandas/instances/42", link.toInstance(42));
        assertEquals("https://admin.example/pandas/login/check-session-reply", link.checkSessionReply());
    }
}
