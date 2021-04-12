package pandas.gatherer.heritrix;

import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;
import pandas.gather.Instance;
import pandas.gatherer.core.Config;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;

class CrawlBeans {
    private static final XPathFactory xPathFactory = XPathFactory.newInstance();
    private static final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    private static final SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();

    static {
        docBuilderFactory.setNamespaceAware(true);
        namespaceContext.bindNamespaceUri("beans", "http://www.springframework.org/schema/beans");
        namespaceContext.bindDefaultNamespaceUri("http://www.springframework.org/schema/beans");
    }

    private static void setBeanProperty(Document doc, String bean, String property, String value) {
        try {
            XPath xPath = xPathFactory.newXPath();
            xPath.setNamespaceContext(namespaceContext);
            XPathExpression expr = xPath.compile(
                    "/beans:beans/beans:bean[@id='" + bean + "']/beans:property[@name='" + property + "']");
            Element element = (Element) expr.evaluate(doc, XPathConstants.NODE);
            element.setAttribute("value", value);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeCrawlXml(Config config, Instance instance, Path jobDir) throws IOException {
        Document doc;
        try (InputStream stream = CrawlBeans.class.getResourceAsStream("crawler-beans.cxml")) {
            doc = docBuilderFactory.newDocumentBuilder().parse(stream);
        } catch (SAXException | ParserConfigurationException e) {
            throw new RuntimeException("error parsing crawler-beans.cxml", e);
        }

        setBeanProperty(doc, "metadata", "jobName", instance.getHumanId());
        setBeanProperty(doc, "metadata", "description", instance.getTitle().getName());
        setBeanProperty(doc, "warcWriter", "prefix", instance.getHumanId());
        setBeanProperty(doc, "fetchHttp", "httpBindAddress", config.getGathererBindAddress());

        DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();
        LSOutput lsOutput = domImplementation.createLSOutput();
        try (Writer writer = Files.newBufferedWriter(jobDir.resolve("crawler-beans.cxml"), UTF_8, CREATE, TRUNCATE_EXISTING, WRITE)) {
            lsOutput.setCharacterStream(writer);
            lsSerializer.write(doc, lsOutput);
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

    static void writeConfig(Config config, Instance instance, Path jobDir) throws IOException {
        writeCrawlXml(config, instance, jobDir);
        writeSeeds(instance.getTitle().getAllSeeds(), jobDir);
    }
}
