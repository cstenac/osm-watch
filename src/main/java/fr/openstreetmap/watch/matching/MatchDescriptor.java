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
	
	public double minX = 180.0;
	public double minY = 90.0;
	public double maxX = -180.0;
	public double maxY = -90.0;
	
	public void addNode(NodeDescriptor node, String why) {
		minX = Math.min(minX, node.lon);
		maxX = Math.max(maxX, node.lon);
		minY = Math.min(minY, node.lat);
		maxY = Math.max(maxY, node.lat);
		
		reasons.add(why + ": node <a href=\"http://www.openstreetmap.org/browse/node/" + node.id + "\">" + node.id + "</a>");
		matches = true;
	}
	public void addWay(WayDescriptor way, String why) {
		if (way.line != null) {
			minX = Math.min(minX, way.line.getEnvelopeInternal().getMinX());
			maxX = Math.max(maxX, way.line.getEnvelopeInternal().getMaxX());
			minY = Math.min(minY, way.line.getEnvelopeInternal().getMinY());
			maxY = Math.max(maxY, way.line.getEnvelopeInternal().getMaxY());
		}
		
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
