package pandas.gather;

import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

class HeritrixConfig {
    private static final XPathFactory xPathFactory = XPathFactory.newInstance();
    private static final DocumentBuilderFactory docBuilderFactory;

    static {
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
    }

    String jobName;
    String description;
    List<String> seeds;
    List<String> surts;

    private static void setBeanProperty(Document doc, String bean, String property, String value) {
        try {
            XPathExpression expr = newXPath().compile(
                    "/beans:beans/beans:bean[@id='" + bean + "']/beans:property[@name='" + property + "']");
            Element element = (Element) expr.evaluate(doc, XPathConstants.NODE);
            element.setAttribute("value", value);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
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

    public String beansXml() {
        Document doc;
        try (InputStream stream = HeritrixConfig.class.getResourceAsStream("crawler-beans.cxml")) {
            doc = docBuilderFactory.newDocumentBuilder().parse(stream);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new RuntimeException("error parsing crawler-beans.cxml", e);
        }

        setBeanProperty(doc, "metadata", "jobName", jobName);
        setBeanProperty(doc, "metadata", "description", description);
        setBeanProperty(doc, "warcWriter", "prefix", "test");
//        setBeanProperty(doc, "fetchHttp", "httpBindAddress", config.gathererBindAddress);

        try {
            // seeds
            Element seedsElement = (Element) newXPath().compile("/beans:beans/beans:bean[@id='seeds']/beans:property[@name='textSource']/beans:bean/beans:property/beans:value").evaluate(doc, XPathConstants.NODE);
            seedsElement.setTextContent(String.join("\n", seeds));

            Element surtsElement = (Element) newXPath().compile("/beans:beans/beans:bean[@id='acceptSurts']/beans:property[@name='surtsSource']/beans:bean/beans:property/beans:value").evaluate(doc, XPathConstants.NODE);
            surtsElement.setTextContent(String.join("\n", surts));
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

        DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();
        LSOutput lsOutput = domImplementation.createLSOutput();

        StringWriter sw = new StringWriter();
        lsOutput.setCharacterStream(sw);
        lsSerializer.write(doc, lsOutput);
        return sw.toString();
    }

}
