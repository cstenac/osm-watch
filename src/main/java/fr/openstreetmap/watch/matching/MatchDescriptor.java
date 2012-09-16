package fr.openstreetmap.watch.matching;

import java.util.ArrayList;
import java.util.List;

import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.WayDescriptor;

/** Describes a final match (after spatial filtering) and why it matches */
public class MatchDescriptor {
	public MatchDescriptor(SpatialMatch sm) {
		this.sm = sm;
	}
	
	public List<String> reasons = new ArrayList<String>();
	
	public SpatialMatch sm;
	public boolean matches;
	
	public void addNode(NodeDescriptor node, String why) {
		reasons.add(why + ": node " + node.id);
		matches = true;
	}
	public void addWay(WayDescriptor node, String why) {
		reasons.add(why + ": way " + node.id);
		matches = true;
	}

}
