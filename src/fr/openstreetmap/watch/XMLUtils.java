package fr.openstreetmap.watch;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLUtils {
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
