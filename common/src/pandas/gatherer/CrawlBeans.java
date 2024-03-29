package pandas.gatherer;

import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;
import pandas.gather.Instance;
import pandas.gather.Profile;
import pandas.gather.Scope;
import pandas.gather.TitleGather;
import pandas.util.SURT;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;

public class CrawlBeans {
    private static final XPathFactory xPathFactory = XPathFactory.newInstance();
    private static final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    private static final SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();

    static {
        docBuilderFactory.setNamespaceAware(true);
        namespaceContext.bindNamespaceUri("beans", "http://www.springframework.org/schema/beans");
        namespaceContext.bindDefaultNamespaceUri("http://www.springframework.org/schema/beans");
    }

    private static void setBeanProperty(Document doc, String bean, String property, String value) {
        Element element = xpath(doc, "/beans:beans/beans:bean[@id='" + bean + "']/beans:property[@name='" + property + "']");
        element.setAttribute("value", value);
    }

    private static Element xpath(Document doc, String expression) {
        try {
            XPath xPath = xPathFactory.newXPath();
            xPath.setNamespaceContext(namespaceContext);
            XPathExpression expr = xPath.compile(expression);
            return (Element) expr.evaluate(doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setBeanPropertyMultiline(Document doc, String bean, String property, String value) {
            Element element = xpath(doc, "/beans:beans/beans:bean[@id='" + bean + "']/beans:property[@name='" + property + "']/beans:value");
            element.setTextContent(value);
    }

    public static void writeCrawlXml(Instance instance, Writer writer, String gathererBindAddress) throws IOException {
        Document doc;
        try (InputStream stream = CrawlBeans.class.getResourceAsStream("/pandas/crawlconfig/crawler-beans.cxml")) {
            doc = docBuilderFactory.newDocumentBuilder().parse(stream);
        } catch (SAXException | ParserConfigurationException e) {
            throw new RuntimeException("error parsing crawler-beans.cxml", e);
        }

        setBeanProperty(doc, "metadata", "jobName", instance.getHumanId());
        setBeanProperty(doc, "metadata", "description", instance.getTitle().getName());
        setBeanProperty(doc, "warcWriter", "prefix", instance.getHumanId());
        if (gathererBindAddress != null) {
            setBeanProperty(doc, "fetchHttp", "httpBindAddress", gathererBindAddress);
        }

        StringBuilder overrides = new StringBuilder();

        TitleGather gather = instance.getTitle().getGather();
        Scope scope = gather.getScope();
        if (scope != null) {
            if (scope.getDepth() != null) {
                overrides.append("tooManyHopsDecideRule.maxHops=").append(scope.getDepth()).append("\n");
            }
            if (scope.isIncludeSubdomains()) {
                overrides.append("onDomainsDecideRule.enabled=true\n");
            }
        }

        Profile profile = gather.getActiveProfile();
        if (profile != null) {
            if (profile.getCrawlLimitBytes() != null) {
                overrides.append("crawlLimiter.maxBytesDownload=").append(profile.getCrawlLimitBytes()).append("\n");
            }
            if (profile.getCrawlLimitSeconds() != null) {
                overrides.append("crawlLimiter.maxTimeSeconds=").append(profile.getCrawlLimitSeconds()).append("\n");
            }
            if (profile.getHeritrixConfig() != null) {
                overrides.append(profile.getHeritrixConfig());
                if (!profile.getHeritrixConfig().endsWith("\n")) {
                    overrides.append("\n");
                }
            }
        }

        if (gather.getIgnoreRobotsTxt()) overrides.append("metadata.robotsPolicyName=ignore\n");

        setBeanPropertyMultiline(doc, "simpleOverrides", "properties", overrides.toString());
        xpath(doc, "/beans:beans/beans:bean[@id='seeds']/beans:property[@name='textSource']/beans:bean/beans:property[@name='value']/beans:value")
                .setTextContent(String.join("\n", instance.getTitle().getAllSeeds()));

        // add surts for alternate www/non-www versions of the seed urls
        // this ensures if we crawl www.example.com we follow links to bare example.com
        setAcceptSurts(doc, generateAltWwwSurts(instance.getTitle().getAllSeeds()));

        DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();

        LSOutput lsOutput = domImplementation.createLSOutput();
        lsOutput.setCharacterStream(writer);
        lsSerializer.write(doc, lsOutput);
    }

    /**
     * Sets the acceptSurts bean's surtsSource property to the given set of SURTs.
     */
    private static void setAcceptSurts(Document doc, Set<String> surts) {
        if (surts.isEmpty()) return;
        // <property name="surtsSource">
        Element surtsSource = doc.createElement("property");
        surtsSource.setAttribute("name", "surtsSource");
        //   <bean class="org.archive.spring.ConfigString">
        Element configStringBean = doc.createElement("bean");
        configStringBean.setAttribute("class", "org.archive.spring.ConfigString");
        //     <property name="value">
        Element valueProperty = doc.createElement("property");
        valueProperty.setAttribute("name", "value");
        //       <value>{surts}</value>
        Element value = doc.createElement("value");
        value.setTextContent(String.join("\n", surts));
        //     </property>
        valueProperty.appendChild(value);
        //   </bean>
        configStringBean.appendChild(valueProperty);
        // </property>
        surtsSource.appendChild(configStringBean);
        xpath(doc, "/beans:beans/beans:bean[@id='acceptSurts']").appendChild(surtsSource);
    }

    private static void writeCrawlXml(Instance instance, Path jobDir, String gathererBindAddress) throws IOException {
        try (Writer writer = Files.newBufferedWriter(jobDir.resolve("crawler-beans.cxml"), UTF_8, CREATE, TRUNCATE_EXISTING, WRITE)) {
            writeCrawlXml(instance, writer, gathererBindAddress);
        }
    }

    private static void writeSeeds(List<String> seeds, Path jobDir) throws IOException {
        try (Writer writer = Files.newBufferedWriter(jobDir.resolve("seeds.txt"),
                UTF_8, CREATE,TRUNCATE_EXISTING, WRITE)) {
            for (String seed: seeds) {
                writer.write(seed);
                writer.write('\n');
            }
        }
    }

    public static void writeConfig(Instance instance, Path jobDir, String gathererBindAddress) throws IOException {
        writeCrawlXml(instance, jobDir, gathererBindAddress);
        writeSeeds(instance.getTitle().getAllSeeds(), jobDir);
    }

    /**
     * Generates an alternate URL by adding or removing www. from the hostname.
     */
    public static String generateAltWwwUrl(String url) {
        if (url.startsWith("http://www.")) {
            return "http://" + url.substring("http://www.".length());
        } else if (url.startsWith("https://www.")) {
            return "https://" + url.substring("https://www.".length());
        } else if (url.startsWith("http://")) {
            return "http://www." + url.substring("http://".length());
        } else if (url.startsWith("https://")) {
            return "https://www." + url.substring("https://".length());
        } else {
            return null;
        }
    }

    /**
     * Generates alternate URLS (SURT format) by adding or removing www. from the hostname.
     */
    public static Set<String> generateAltWwwSurts(List<String> urls) {
        var surts = new TreeSet<String>();
        for (String seed : urls) {
            String altUrl = generateAltWwwUrl(seed);
            if (altUrl != null) {
                surts.add("+" + SURT.prefixFromPlainForceHttp(altUrl));
            }
        }
        return surts;
    }
}
