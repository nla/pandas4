package pandas.crawlconfig;

import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * Utilities to simplify working with XML documents.
 */
public class Xml {
    private static final XPathFactory xPathFactory = XPathFactory.newInstance();
    private static final DocumentBuilderFactory docBuilderFactory;

    static {
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
    }

    public static Document parse(String string) {
        try {
            return parse(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Document parse(InputStream stream) throws IOException {
        try {
            return docBuilderFactory.newDocumentBuilder().parse(stream);
        } catch (SAXException | ParserConfigurationException e) {
            throw new IOException(e);
        }
    }

    private static XPath newXPath() {
        XPath xPath = xPathFactory.newXPath();
        SimpleNamespaceContext namespace = new SimpleNamespaceContext();
        namespace.bindNamespaceUri("beans", "http://www.springframework.org/schema/beans");
        namespace.bindDefaultNamespaceUri("http://www.springframework.org/schema/beans");
        xPath.setNamespaceContext(namespace);
        return xPath;
    }

    public static Element select(Document document, String xpath) {
        Element element = selectOrNull(document, xpath);
        if (element == null) {
            throw new RuntimeException("No match: " + xpath);
        }
        return element;
    }

    public static Element selectOrNull(Document document, String xpath) {
        try {
            return (Element) newXPath().compile(xpath).evaluate(document, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toString(Document doc) {
        DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();
        LSOutput lsOutput = domImplementation.createLSOutput();

        StringWriter sw = new StringWriter();
        lsOutput.setCharacterStream(sw);
        lsSerializer.write(doc, lsOutput);
        return sw.toString();
    }
}
