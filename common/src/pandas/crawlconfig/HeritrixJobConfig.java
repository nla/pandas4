package pandas.crawlconfig;

import org.netpreserve.urlcanon.Canonicalizer;
import org.netpreserve.urlcanon.ParsedUrl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

public class HeritrixJobConfig {
    private final String name;
    private final String userAgent;
    private final List<String> seeds;
    private final List<String> surts;

    public HeritrixJobConfig(CrawlConfig crawlConfig) {
        name = crawlConfig.getId();
        userAgent = crawlConfig.getUserAgent();
        seeds = crawlConfig.getSeeds().stream()
                .map(seed -> parseAndCanonicalize(seed.getUrl()).toString())
                .sorted()
                .distinct()
                .toList();
        surts = crawlConfig.getSeeds().stream()
                .map(seed -> urlToSurt(seed.getUrl(), seed.getScope()))
                .sorted()
                .distinct()
                .toList();
    }

    public static ParsedUrl parseAndCanonicalize(String url) {
        ParsedUrl parsedUrl = ParsedUrl.parseUrl(url);

        // default to http:// if there's no scheme
        if (parsedUrl.getScheme().isEmpty()) {
            parsedUrl = ParsedUrl.parseUrl("http://" + url);
        }

        Canonicalizer.WHATWG.canonicalize(parsedUrl);
        return parsedUrl;
    }

    public static String urlToSurt(String url, Scope scope) {
        ParsedUrl parsedUrl = parseAndCanonicalize(url);
        if (scope == Scope.AUTO) {
            scope = parsedUrl.getPath().equals("/") ? Scope.DOMAIN : Scope.DIRECTORY;
        }
        switch (scope) {
            case PAGE:
                return parsedUrl.surt();
            case DIRECTORY:
                parsedUrl.setFragment("");
                parsedUrl.setHashSign("");
                parsedUrl.setQuery("");
                parsedUrl.setQuestionMark("");
                parsedUrl.setPath(parsedUrl.getPath().replaceFirst("[^/]+$", ""));
                return parsedUrl.surt();
            case HOST:
                parsedUrl.setFragment("");
                parsedUrl.setHashSign("");
                parsedUrl.setQuery("");
                parsedUrl.setQuestionMark("");
                parsedUrl.setPath("/");
                return parsedUrl.surt();
            case DOMAIN:
                parsedUrl.setFragment("");
                parsedUrl.setHashSign("");
                parsedUrl.setQuery("");
                parsedUrl.setQuestionMark("");
                parsedUrl.setPath("");
                parsedUrl.setPort("");
                parsedUrl.setColonBeforePort("");
                if (parsedUrl.getHost().startsWith("www.")) {
                    parsedUrl.setHost(parsedUrl.getHost().substring("www.".length()));
                }
                return parsedUrl.surt().replaceFirst("\\)$", "");
            default:
                throw new IllegalArgumentException("unimplemented scope: " + scope);
        }
    }

    private static void setBeanProperty(Document doc, String bean, String name, String value) {
        Element property = Xml.selectOrNull(doc, "/beans:beans/beans:bean[@id='" + bean + "']/beans:property[@name='" + name + "']");
        if (property == null) {
            Element beanElement = Xml.select(doc, "/beans:beans/beans:bean[@id='" + bean + "']");
            property = doc.createElement("property");
            property.setAttribute("name", name);
            beanElement.appendChild(property);
        }
        property.setAttribute("value", value);
    }

    public String toXml() {
        Document doc;
        try (InputStream stream = HeritrixJobConfig.class.getResourceAsStream("heritrix-beans.cxml")) {
            doc = Xml.parse(stream);
        } catch (IOException e) {
            throw new RuntimeException("error parsing crawler-beans.cxml", e);
        }


        Element longerOverrides = Xml.select(doc, "/beans:beans/beans:bean[@id='longerOverrides']");
        longerOverrides.getParentNode().removeChild(longerOverrides);

        Properties globals = new Properties();
        try (InputStream stream = getClass().getResourceAsStream("heritrix-defaults.properties")) {
            globals.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Error loading heritirx-default.properties", e);
        }

        globals.put("metadata.jobName", name);
        if (userAgent != null) {
            globals.put("metadata.userAgentTemplate", userAgent);
        }
        globals.put("warcWriter.prefix", name);

        Element simpleOverrides = Xml.select(doc, "/beans:beans/beans:bean[@id='simpleOverrides']/beans:property/beans:value");
        StringWriter writer = new StringWriter();
        try {
            globals.store(writer, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        simpleOverrides.setTextContent(writer.toString().replaceFirst("^#.*", ""));

        // seeds
        Element seedsElement = Xml.select(doc, "/beans:beans/beans:bean[@id='seeds']/beans:property[@name='textSource']/beans:bean/beans:property/beans:value");
        seedsElement.setTextContent(String.join("\n", seeds));

        // surts
        Element acceptSurtsBean = Xml.select(doc, "/beans:beans/beans:bean[@id='acceptSurts']");
        Element property = Xml.parse("<property xmlns='http://www.springframework.org/schema/beans' name='surtsSource'>" +
                "<bean class='org.archive.spring.ConfigString'>" +
                "<property name='value'><value></value></property></bean></property>").getDocumentElement();
        property.removeAttribute("xmlns");
        Node node = doc.importNode(property, true);
        acceptSurtsBean.appendChild(node);

        Element surtsElement = Xml.select(doc, "/beans:beans/beans:bean[@id='acceptSurts']/beans:property[@name='surtsSource']/beans:bean/beans:property/beans:value");
        surtsElement.setTextContent(String.join("\n", surts));

        return Xml.toString(doc);
    }
}
