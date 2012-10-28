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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import fr.openstreetmap.watch.model.ChangesetDescriptor;
import fr.openstreetmap.watch.model.NodeChange;
import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.WayChange;
import fr.openstreetmap.watch.model.WayDescriptor;
import fr.openstreetmap.watch.util.XMLUtils;
import fr.openstreetmap.watch.util.XMLUtils.ElementIterable;


public class AugmentedDiffV2Parser implements DiffParser{
	private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	XPathFactory xPathfactory = XPathFactory.newInstance();

	Map<Long, ChangesetDescriptor> changesets = new HashMap<Long, ChangesetDescriptor>();

	/* These nodes haven't changed, they are used to reconstruct way geometry */
	Map<Long, NodeDescriptor> keptNodes = new HashMap<Long, NodeDescriptor>();
	/*  These ways haven't chnaged, they are used to reconstruct relations */
	Map<Long, WayDescriptor> keptWays = new HashMap<Long, WayDescriptor>();

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

	protected Element getChildElement(Element node, int index) {
		NodeList nl = node.getChildNodes();
		int nextElement = 0;
		for (int i = 0; i < nl.getLength(); i++) {
			Node child = nl.item(i);
			if (child instanceof Element) {
				if (nextElement == index) return (Element)child;
				nextElement++;
			}
		}
		throw new IllegalArgumentException("Failed to find child element " + index +  " of " + node);
	}

	protected void parseActions(XPath xpath, Document doc) throws Exception {
		XPathExpression expr = xpath.compile("/osmAugmentedDiff/action");
		NodeList nl = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
		for (Element e : new XMLUtils.ElementIterable(nl)) {
			String type = e.getAttribute("type");
			//            System.out.println("E TAGS= " + e.getAttribute("tags"));
			//            System.out.println("E MEMBERS= " + e.getAttribute("members"));

			if (type.equals("info")) {
				Element child = getChildElement(e, 0);
				if (child.getNodeName().equals("node")) {
					NodeDescriptor nd = parseNode(child);
					keptNodes.put(nd.id, nd);
				} else if (child.getNodeName().equals("way")) {
					WayDescriptor wd = parseWay(child, true);
					keptWays.put(wd.id, wd);
				} else {
					logger.info("Unknown child in type=info: " + child.getNodeName());
				}
			} else if (type.equals("create")) {
				Element child = getChildElement(e, 0);
				if (child.getNodeName().equals("node")) {
					NodeDescriptor nd = parseNode(child);
					newNodes.put(nd.id, nd);
				} else if (child.getNodeName().equals("way")) {
					WayDescriptor wd = parseWay(child, true);
					newWays.put(wd.id, wd);
				} else {
					logger.info("Unknown child in type=create: " + child.getNodeName());
				}
			} else if (type.equals("delete")) {
				Element childOld = getChildElement(e, 0);
				Preconditions.checkArgument(childOld.getNodeName().equals("old"));
				Element childNew = getChildElement(e, 1);
				Preconditions.checkArgument(childNew.getNodeName().equals("new"));

				Element objInOld = getChildElement(childOld, 0);
				Element objInNew = getChildElement(childNew, 0);

				if (objInOld.getNodeName().equals("node")) {
					NodeDescriptor nd = parseNode(objInOld);
					/* Override data with the correct timestamp, changeset, uid and version from the "new" node */ 
					nd.uid = Long.parseLong(objInNew.getAttribute("uid"));
					nd.changeset = Long.parseLong(objInNew.getAttribute("changeset"));
					nd.version= Long.parseLong(objInNew.getAttribute("version"));
					//                    nd.timestamp = Long.parseLong(objInNew.getAttribute("timestamp"));
					reallyDeletedNodes.put(nd.id, nd);
				} else if (objInOld.getNodeName().equals("way")) {
					WayDescriptor wd = parseWay(objInOld, true);
					/* Override data with the correct timestamp, changeset, uid and version from the "new" node */ 
					wd.uid = Long.parseLong(objInNew.getAttribute("uid"));
					wd.changeset = Long.parseLong(objInNew.getAttribute("changeset"));
					wd.version= Long.parseLong(objInNew.getAttribute("version"));
					//                    nd.timestamp = Long.parseLong(objInNew.getAttribute("timestamp"));
					reallyDeletedWays.put(wd.id, wd);
				}
			} else if (type.equals("modify")) {
				Element childOld = getChildElement(e, 0);
				Preconditions.checkArgument(childOld.getNodeName().equals("old"));
				Element childNew = getChildElement(e, 1);
				Preconditions.checkArgument(childNew.getNodeName().equals("new"));

				System.out.println("childOld=" + childOld);
				System.out.println("childNew=" + childNew);

				System.out.println("CN Child1=" + ((Element)childNew.getChildNodes().item(1)).getAttribute("changeset"));

				Element objInOld = getChildElement(childOld, 0);
				Element objInNew = getChildElement(childNew, 0);

				if (objInOld.getNodeName().equals("node")) {
					NodeDescriptor oldNode  = parseNode(objInOld);
					NodeDescriptor newNode = parseNode(objInNew);
					NodeChange nc = new NodeChange(oldNode, newNode);
					changedNodes.put(oldNode.id, nc);
				} else if (objInOld.getNodeName().equals("way")) {
					/* Try to see if this way has truly been changed. In that case, the interesting changeset
					 * is the "after".
					 * Else, if only nodes have been moved or tagged (but not added, removed, and no tags changed on the
					 * way), both the "before" and "after" for this way will be the 'old' changeset of the way. We then
					 * need to find the 'new' changeset by taking the one of the NodeChange object.
					 */
					if (!StringUtils.isEmpty(e.getAttribute("tags")) ||
							!StringUtils.isEmpty(e.getAttribute("members"))) {
						// Real change of the way
						WayDescriptor oldWay  = parseWay(objInOld, true);
						WayDescriptor newWay = parseWay(objInNew, true);
						WayChange nc = new WayChange(oldWay, newWay);
						changedWays.put(oldWay.id, nc);
					} else {
						// No real change of the way, find the nodes.
						WayDescriptor oldWay  = parseWay(objInOld, true);
						WayDescriptor newWay = parseWay(objInNew, true);
						WayChange wc = new WayChange(oldWay, newWay);
						for (Element e2 : new ElementIterable(objInNew.getChildNodes())) {
							if (e2.getNodeName().equals("nd")) {
								long nodeId = Long.parseLong(e2.getAttribute("ref"));
								NodeChange nc = changedNodes.get(nodeId);
								if (nc != null) {
									logger.info("Found NodeChange for way: " + newWay.id + " -> " + nc.changeset);
									wc.changeset = nc.changeset;
									wc.after.user = nc.after.user;
									wc.after.uid = nc.after.uid;
									break;
								}
							}
						}
						changedWays.put(oldWay.id, wc);
					}
				}
			} else {
				logger.info("Unknown type " + type);
			}
		}
	}

	public void parse(String oscFileContent) throws Exception {
		oscFileContent =  oscFileContent.replace("&", "&amp;");
		InputSource is = new InputSource(new StringReader(oscFileContent));
		DocumentBuilder db = dbf.newDocumentBuilder();

		logger.info("Parsing XML");
		Document doc = db.parse(is);
		XPath xpath = xPathfactory.newXPath();

		logger.info("XML parsed, handling actions");
		parseActions(xpath, doc);
		logger.info("Computing changesets");
		groupStuffByChangeset();
		logger.info("Parsing done");
	}

	private ChangesetDescriptor getChangeset(long id) {

		if (changesets.containsKey(id)) {
			return changesets.get(id);
		} else {
			System.out.println("GET CHANGESET " + id);
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
			System.out.println("WC c=" + nc.changeset);
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
		nd.user = e.getAttribute("user");

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
		nd.user = e.getAttribute("user");

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
				try {
					nd.line = factory.createLineString(coordinates.toArray(new Coordinate[0]));
				} catch (IllegalArgumentException e2) {
					logger.error("Failed to construct line string for way " + nd.id + ": " + e2.getMessage());
				}
			}
		}

		return nd;
	}

	private static Logger logger = Logger.getLogger("osm.watch.parser");
	private static GeometryFactory factory = new GeometryFactory();
}
