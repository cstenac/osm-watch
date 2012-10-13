package fr.openstreetmap.watch.model;
import java.util.HashMap;
import java.util.Map;

import fr.openstreetmap.watch.model.db.Changeset;


public class ChangesetDescriptor {
	public long id;
	
	public Changeset toDBModel() {
		Changeset out =new Changeset();
		out.setId(id);
		out.setUserName(user);
		out.setMaxX(maxX);
		out.setMinX(minX);
		out.setMaxY(maxY);
		out.setMinY(minY);
		
		out.setDeletedNodes(deletedNodes.size());
		out.setNewNodes(newNodes.size());
		out.setChangedNodes(changedNodes.size());
		out.setDeletedWays(deletedWays.size());
		out.setNewWays(newWays.size());
		out.setChangedWays(changedWays.size());
		out.setWaysWithChangedNodes(waysWithChangedNodes.size());
		return out;
	}
	
	public void computeUser() {
		for (NodeDescriptor nd : deletedNodes.values()) {
			user = nd.getUser();
			return;
		}
		for (NodeDescriptor nd : newNodes.values()) {
			user = nd.getUser();
			return;
		}
		for (NodeChange nc : changedNodes.values()) {
			user = nc.after.getUser();
			return;
		}
		for (WayDescriptor nd : deletedWays.values()) {
			user = nd.getUser();
			return;
		}
		for (WayDescriptor nd : newWays.values()) {
			user = nd.getUser();
			return;
		}
		for (WayChange nc : changedWays.values()) {
			user = nc.after.getUser();
			return;
		}
	}
	
	String user;
	double minX = 180.0, minY = 90.0, maxX = -180.0, maxY = -90.0;
	
	public void computeBBox() {
		for (NodeDescriptor nd : deletedNodes.values()) {
			minX = Math.min(minX, nd.lon);maxX = Math.max(minX, nd.lon);
			minY = Math.min(minY, nd.lat);maxY = Math.max(minY, nd.lat);
		}
		for (NodeDescriptor nd : newNodes.values()) {
			minX = Math.min(minX, nd.lon);maxX = Math.max(minX, nd.lon);
			minY = Math.min(minY, nd.lat);maxY = Math.max(minY, nd.lat);
		}
		for (NodeChange nc : changedNodes.values()) {
			minX = Math.min(minX, nc.before.lon);maxX = Math.max(minX, nc.before.lon);
			minY = Math.min(minY, nc.before.lat);maxY = Math.max(minY, nc.before.lat);
			minX = Math.min(minX, nc.after.lon);maxX = Math.max(minX, nc.after.lon);
			minY = Math.min(minY, nc.after.lat);maxY = Math.max(minY, nc.after.lat);
		}
		for (WayDescriptor nd : deletedWays.values()) {
			if (nd.line == null) continue;
			minX = Math.min(minX, nd.line.getEnvelopeInternal().getMinX());
			maxX = Math.max(maxX, nd.line.getEnvelopeInternal().getMaxX());
			minY = Math.min(minY, nd.line.getEnvelopeInternal().getMinY());
			maxY = Math.max(maxY, nd.line.getEnvelopeInternal().getMaxY());
		}
		for (WayDescriptor nd : newWays.values()) {
			if (nd.line == null) continue;
			minX = Math.min(minX, nd.line.getEnvelopeInternal().getMinX());
			maxX = Math.max(maxX, nd.line.getEnvelopeInternal().getMaxX());
			minY = Math.min(minY, nd.line.getEnvelopeInternal().getMinY());
			maxY = Math.max(maxY, nd.line.getEnvelopeInternal().getMaxY());
		}
		for (WayDescriptor nd : waysWithChangedNodes.values()) {
			if (nd.line == null) continue;
			minX = Math.min(minX, nd.line.getEnvelopeInternal().getMinX());
			maxX = Math.max(maxX, nd.line.getEnvelopeInternal().getMaxX());
			minY = Math.min(minY, nd.line.getEnvelopeInternal().getMinY());
			maxY = Math.max(maxY, nd.line.getEnvelopeInternal().getMaxY());
		}
		for (WayChange wc : changedWays.values()) {
			if (wc.before.line == null) continue;
			minX = Math.min(minX, wc.before.line.getEnvelopeInternal().getMinX());
			maxX = Math.max(maxX, wc.before.line.getEnvelopeInternal().getMaxX());
			minY = Math.min(minY, wc.before.line.getEnvelopeInternal().getMinY());
			maxY = Math.max(maxY, wc.before.line.getEnvelopeInternal().getMaxY());
			if (wc.after.line == null) continue;
			minX = Math.min(minX, wc.after.line.getEnvelopeInternal().getMinX());
			maxX = Math.max(maxX, wc.after.line.getEnvelopeInternal().getMaxX());
			minY = Math.min(minY, wc.after.line.getEnvelopeInternal().getMinY());
			maxY = Math.max(maxY, wc.after.line.getEnvelopeInternal().getMaxY());
		}
	}
    
    public Map<Long, NodeDescriptor> deletedNodes = new HashMap<Long, NodeDescriptor>();
    public Map<Long, NodeChange> changedNodes = new HashMap<Long, NodeChange>();
    public Map<Long, NodeDescriptor> newNodes = new HashMap<Long, NodeDescriptor>();
    
    public Map<Long, WayDescriptor> waysWithChangedNodes = new HashMap<Long, WayDescriptor>();// TODO DODO 
    public Map<Long, WayDescriptor> deletedWays = new HashMap<Long, WayDescriptor>();
    public Map<Long, WayChange> changedWays = new HashMap<Long, WayChange>(); 
    public Map<Long, WayDescriptor> newWays = new HashMap<Long, WayDescriptor>();
}