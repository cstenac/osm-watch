package fr.openstreetmap.watch.model;

import java.util.ArrayList;
import java.util.List;

/** Describes what matches and why */
public class MatchDescriptor {
	static public class MatchingNode {
		public NodeDescriptor node;
		public String why;
	}
	public boolean matches;
	
	public List<MatchingNode> nodes = new ArrayList<MatchingNode>();
	
	public void addNode(NodeDescriptor node, String why) {
		MatchingNode m =new MatchingNode();
		m.node = node;
		m.why = why;
		nodes.add(m);
		matches = true;
	}
}
