package fr.openstreetmap.watch.parsers;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import fr.openstreetmap.watch.XMLUtils;
import fr.openstreetmap.watch.XMLUtils.ElementIterable;
import fr.openstreetmap.watch.model.ChangesetDescriptor;
import fr.openstreetmap.watch.model.NodeChange;
import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.WayChange;
import fr.openstreetmap.watch.model.WayDescriptor;


public class AugmentedDiffParser {
    private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    XPathFactory xPathfactory = XPathFactory.newInstance();
    
    Map<Long, ChangesetDescriptor> changesets = new HashMap<Long, ChangesetDescriptor>();

    /* These nodes haven't changed, they are used to reconstruct way geometry */
    Map<Long, NodeDescriptor> keptNodes = new HashMap<Long, NodeDescriptor>();
    
    Map<Long, NodeDescriptor> reallyDeletedNodes = new HashMap<Long, NodeDescriptor>();
    Map<Long, NodeChange> changedNodes = new HashMap<Long, NodeChange>();
    Map<Long, NodeDescriptor> newNodes = new HashMap<Long, NodeDescriptor>();
    
    Map<Long, WayDescriptor> waysWithChangedNodes = new HashMap<Long, WayDescriptor>();
    Map<Long, WayDescriptor> reallyDeletedWays = new HashMap<Long, WayDescriptor>();
    Map<Long, WayChange> changedWays = new HashMap<Long, WayChange>();
    Map<Long, WayDescriptor> newWays = new HashMap<Long, WayDescriptor>();
    
    public Map<Long, ChangesetDescriptor> getChangesets() {
        return changesets;
    }
    
    protected void parseNodesSections(XPath xpath, Document doc) throws Exception{
        Map<Long, NodeDescriptor> maybeDeletedNodes = new HashMap<Long, NodeDescriptor>();
        Map<Long, NodeDescriptor> maybeNewNodes = new HashMap<Long, NodeDescriptor>();
        {
            XPathExpression expr = xpath.compile("/osm/delete/node");
            NodeList nl = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            for (Element e : new XMLUtils.ElementIterable(nl)) {
                NodeDescriptor nd = parseNode(e);
                maybeDeletedNodes.put(nd.id, nd);
            }
        }

        {
            XPathExpression expr = xpath.compile("/osm/keep/node");
            NodeList nl = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            for (Element e : new XMLUtils.ElementIterable(nl)) {
                NodeDescriptor nd = parseNode(e);
                keptNodes.put(nd.id, nd);
            }
        }
        {
            XPathExpression expr = xpath.compile("/osm/insert/node");
            NodeList nl = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            for (Element e : new XMLUtils.ElementIterable(nl)) {
                NodeDescriptor nd = parseNode(e);
                maybeNewNodes.put(nd.id, nd);
            }
        }
        /* Now, collate the possibly deleted nodes */
        for (NodeDescriptor nd : maybeNewNodes.values()) {
            if (maybeDeletedNodes.containsKey(nd.id)) {
                /* This node is modified, create the change */
                NodeDescriptor oldVersion = maybeDeletedNodes.get(nd.id);
                NodeChange nc = new NodeChange(oldVersion, nd);
                changedNodes.put(nd.id, nc);
                maybeDeletedNodes.remove(nd.id);
            } else {
                /* This node is truly new */
                newNodes.put(nd.id, nd);
            }
        }
        /* The nodes remaining in maybeDeletedNodes are really deleted */
        reallyDeletedNodes.putAll(maybeDeletedNodes);
    }
    
    protected void parseWaysSections(XPath xpath, Document doc) throws Exception{
        Map<Long, WayDescriptor> maybeDeletedWays = new HashMap<Long, WayDescriptor>();
        Map<Long, WayDescriptor> maybeNewWays = new HashMap<Long, WayDescriptor>();
        {
            XPathExpression expr = xpath.compile("/osm/delete/way");
            NodeList nl = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            for (Element e : new XMLUtils.ElementIterable(nl)) {
                WayDescriptor wd = parseWay(e);
                maybeDeletedWays.put(wd.id, wd);
            }
        }
        {
            XPathExpression expr = xpath.compile("/osm/keep/way");
            NodeList nl = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            for (Element e : new XMLUtils.ElementIterable(nl)) {
                WayDescriptor wd = parseWay(e);
                waysWithChangedNodes.put(wd.id, wd);
            }
        }
        {
            XPathExpression expr = xpath.compile("/osm/insert/way");
            NodeList nl = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            for (Element e : new XMLUtils.ElementIterable(nl)) {
                WayDescriptor wd = parseWay(e);
                maybeNewWays.put(wd.id, wd);
            }
        }

        for (WayDescriptor wd : maybeNewWays.values()) {
            if (maybeDeletedWays.containsKey(wd.id)) {
                /* This way is modified, create the change */
                WayDescriptor oldVersion = maybeDeletedWays.get(wd.id);
                WayChange wc = new WayChange(oldVersion, wd);
                changedWays.put(wd.id, wc);
                maybeDeletedWays.remove(wd.id);
            } else {
                /* This way is truly new */
                newWays.put(wd.id, wd);
            }
        }
        /* The ways remaining in maybeDeletedWays are really deleted */
        reallyDeletedWays.putAll(maybeDeletedWays);
    }

    
    public void parse(String oscFileContent) throws Exception {
        InputSource is = new InputSource(new StringReader(oscFileContent));
        DocumentBuilder db = dbf.newDocumentBuilder();
        
        System.out.println("Parsing");
        Document doc = db.parse(is);
        System.out.println("Parsed");
        XPath xpath = xPathfactory.newXPath();
    
        System.out.println("Handling nodes");
        parseNodesSections(xpath, doc);
        System.out.println("Handling wyas");
        parseWaysSections(xpath, doc);
        System.out.println("Grouping");
        groupStuffByChangeset();
        
    }
    
    private ChangesetDescriptor getChangeset(long id) {
        if (changesets.containsKey(id)) {
            return changesets.get(id);
        } else {
            ChangesetDescriptor cd = new ChangesetDescriptor();
            cd.id = id;
            changesets.put(id, cd);
            return cd;
        }
    }
    
    private void groupStuffByChangeset() {
        for (NodeDescriptor nd : reallyDeletedNodes.values()) {
            getChangeset(nd.changeset).deletedNodes.put(nd.id, nd);
        }
        for (NodeChange nc : changedNodes.values()) {
            getChangeset(nc.changeset).changedNodes.put(nc.id, nc);
        }
        for (NodeDescriptor nd : newNodes.values()) {
            getChangeset(nd.changeset).newNodes.put(nd.id, nd);
        }
        
        // TODO: Handle "waysWithChangedNodes"
        
        for (WayDescriptor nd : reallyDeletedWays.values()) {
            getChangeset(nd.changeset).deletedWays.put(nd.id, nd);
        }
        for (WayChange nc : changedWays.values()) {
            getChangeset(nc.changeset).changedWays.put(nc.id, nc);
        }
        for (WayDescriptor nd : newWays.values()) {
            getChangeset(nd.changeset).newWays.put(nd.id, nd);
        }
    }
    
    private NodeDescriptor parseNode(Element e) {
        NodeDescriptor nd = new NodeDescriptor();
        nd.id = Long.parseLong(e.getAttribute("id"));
        nd.lat = Double.parseDouble(e.getAttribute("lat"));
        nd.lon = Double.parseDouble(e.getAttribute("lon"));
        nd.version = Long.parseLong(e.getAttribute("version"));
        //nd.timestamp = Long.parseLong(e.getAttribute("id"));
        nd.changeset = Long.parseLong(e.getAttribute("changeset"));
        nd.uid = Long.parseLong(e.getAttribute("uid"));
        
        if (e.getChildNodes().getLength() > 0) {
            nd.tags = new HashMap<String, String>();
            for (Element tagElt : new XMLUtils.ElementIterable(e.getChildNodes())) {
                nd.tags.put(tagElt.getAttribute("k"), tagElt.getAttribute("v"));
            }
        }
        
        return nd;
    }
    private WayDescriptor parseWay(Element e) {
        WayDescriptor nd = new WayDescriptor();
        nd.id = Long.parseLong(e.getAttribute("id"));
        nd.version = Long.parseLong(e.getAttribute("version"));
        //nd.timestamp = Long.parseLong(e.getAttribute("id"));
        nd.changeset = Long.parseLong(e.getAttribute("changeset"));
        nd.uid = Long.parseLong(e.getAttribute("uid"));
        
        if (e.getChildNodes().getLength() > 0) {
            nd.tags = new HashMap<String, String>();
            for (Element childElt : new XMLUtils.ElementIterable(e.getChildNodes())) {
                if (childElt.getNodeName().equals("nd")) {
                    nd.nodes.add(Long.parseLong(childElt.getAttribute("ref")));
                } else {
                    nd.tags.put(childElt.getAttribute("k"), childElt.getAttribute("v"));
                }
            }
        }
        
        return nd;
    }
}
