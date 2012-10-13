package fr.openstreetmap.watch.matching;

import java.util.ArrayList;
import java.util.List;

import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.WayDescriptor;

/** Describes a final match (after spatial filtering) and why it matches */
public class MatchDescriptor {
	public MatchDescriptor(SpatialMatch spatialMatch) {
		this.spatialMatch = spatialMatch;
	}
	
	public List<String> reasons = new ArrayList<String>();
	private SpatialMatch spatialMatch;
	public boolean matches;
	
	public void addNode(NodeDescriptor node, String why) {
		reasons.add(why + ": node <a href=\"http://www.openstreetmap.org/browse/node/" + node.id + "\">" + node.id + "</a>");
		matches = true;
	}
	public void addWay(WayDescriptor way, String why) {
		reasons.add(why + ": way <a href=\"http://www.openstreetmap.org/browse/way/" + way.id + "\">" + way.id + "</a>");
		matches = true;
	}
	public SpatialMatch getSpatialMatch() {
		return spatialMatch;
	}
	public void setSpatialMatch(SpatialMatch spatialMatch) {
		this.spatialMatch = spatialMatch;
	}
}
