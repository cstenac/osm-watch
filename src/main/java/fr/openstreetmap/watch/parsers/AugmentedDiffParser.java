package fr.openstreetmap.watch.parsers;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import fr.openstreetmap.watch.model.ChangesetDescriptor;
import fr.openstreetmap.watch.model.NodeChange;
import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.WayChange;
import fr.openstreetmap.watch.model.WayDescriptor;
import fr.openstreetmap.watch.util.XMLUtils;
import fr.openstreetmap.watch.util.XMLUtils.ElementIterable;


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
            XPathExpression expr = xpath.compile("/osmAugmentedDiff/erase/node");
            NodeList nl = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            for (Element e : new XMLUtils.ElementIterable(nl)) {
                NodeDescriptor nd = parseNode(e);
                maybeDeletedNodes.put(nd.id, nd);
            }
        }

        {
            XPathExpression expr = xpath.compile("/osmAugmentedDiff/keep/node");
            NodeList nl = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            for (Element e : new XMLUtils.ElementIterable(nl)) {
                NodeDescriptor nd = parseNode(e);
                keptNodes.put(nd.id, nd);
            }
        }
        {
            XPathExpression expr = xpath.compile("/osmAugmentedDiff/insert/node");
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
            XPathExpression expr = xpath.compile("/osmAugmentedDiff/erase/way");
            NodeList nl = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            for (Element e : new XMLUtils.ElementIterable(nl)) {
                WayDescriptor wd = parseWay(e, true);
                maybeDeletedWays.put(wd.id, wd);
            }
        }
        {
            XPathExpression expr = xpath.compile("/osmAugmentedDiff/keep/way");
            NodeList nl = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            for (Element e : new XMLUtils.ElementIterable(nl)) {
                WayDescriptor wd = parseWay(e, true);
                waysWithChangedNodes.put(wd.id, wd);
            }
        }
        {
            XPathExpression expr = xpath.compile("/osmAugmentedDiff/insert/way");
            NodeList nl = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            for (Element e : new XMLUtils.ElementIterable(nl)) {
                WayDescriptor wd = parseWay(e, true);
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
        oscFileContent =  oscFileContent.replace("&", "&amp;");
        InputSource is = new InputSource(new StringReader(oscFileContent));
        DocumentBuilder db = dbf.newDocumentBuilder();

        logger.info("Parsing XML");
        Document doc = db.parse(is);
        XPath xpath = xPathfactory.newXPath();

        logger.info("XML parsed, handling nodes sections");
        parseNodesSections(xpath, doc);
        logger.info("Handling ways sections");
        parseWaysSections(xpath, doc);
        logger.info("Computing changesets");
        groupStuffByChangeset();
        logger.info("Parsing done");
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
    private WayDescriptor parseWay(Element e, boolean computeGeom) {
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


        if (computeGeom) {
            List<Coordinate> coordinates = new ArrayList<Coordinate>();
            boolean canConstruct = true;
            for (long id : nd.nodes) {
                NodeDescriptor foundNode = null;
                if (newNodes.containsKey(id)) {
                    foundNode = newNodes.get(id);
                } else if (changedNodes.containsKey(id)) {
                    foundNode = changedNodes.get(id).after;
                } else if (reallyDeletedNodes.containsKey(id)) {
                    foundNode = reallyDeletedNodes.get(id);
                } else if (keptNodes.containsKey(id)) {
                    foundNode = keptNodes.get(id);
                }
                if (foundNode == null) {
                    logger.error("Failed to find node " + id + " to reconstruct way " + nd.id);
                    canConstruct = false;
                    break;
                }
                coordinates.add(new Coordinate(foundNode.lon, foundNode.lat));
            }
            if (canConstruct) {
                nd.line = factory.createLineString(coordinates.toArray(new Coordinate[0]));
            }
        }

        return nd;
    }

    private static Logger logger = Logger.getLogger("osm.watch.parser");
    private static GeometryFactory factory = new GeometryFactory();
}
