package fr.openstreetmap.watch.util;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLUtils {
    static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    static XPathFactory xPathfactory = XPathFactory.newInstance();
    
    public static Document parse(String content) throws SAXException, IOException, ParserConfigurationException {
        return dbf.newDocumentBuilder().parse(new InputSource(new StringReader(content)));
    }
    
    public static NodeIterable xpath(Document d, String expr) throws XPathExpressionException {
        XPath xpath = xPathfactory.newXPath();
        XPathExpression xpathExpr = xpath.compile(expr);
        NodeList nl = (NodeList)xpathExpr.evaluate(d, XPathConstants.NODESET);
        return new NodeIterable(nl);
    }
    
    public static class NodeIterable implements Iterable<Node> {
        public NodeIterable(NodeList ndl) {
            this.ndl = ndl;
        }

        NodeList ndl;

        @Override
        public Iterator<Node> iterator() {
            return new NodeIterator(ndl);
        }
    }

    public static class NodeIterator implements Iterator<Node> {
        public NodeIterator(NodeList ndl) {
            this.ndl = ndl;
        }

        int next = 0;
        NodeList ndl;

        @Override
        public boolean hasNext() {
            return next < (ndl.getLength());
        }

        @Override
        public Node next() {
            return ndl.item(next++);
        }

        @Override
        public void remove() {
        }
    }

    /**
     * Iterable on a NodeList, that only returns the elements.
     */
    public static class ElementIterable implements Iterable<Element> {
        public ElementIterable(NodeList ndl) {
            for (Node n : new NodeIterable(ndl)) {
                if (n instanceof Element) {
                    elts.add((Element)n);
                }
            }
        }

        List<Element> elts = new ArrayList<Element>();

        @Override
        public Iterator<Element> iterator() {
            return elts.iterator();
        }
    }
}
