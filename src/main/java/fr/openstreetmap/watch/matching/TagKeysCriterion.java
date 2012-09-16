package fr.openstreetmap.watch.matching;

import java.util.Set;

import fr.openstreetmap.watch.model.NodeChange;
import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.WayChange;
import fr.openstreetmap.watch.model.WayDescriptor;

public class TagKeysCriterion extends Criterion {
	Set<String> watchedKeys;
	public TagKeysCriterion(Set<String> watchedKeys) {
		this.watchedKeys = watchedKeys;
	}
	@Override
	public MatchDescriptor matches(SpatialMatch sm) {
		MatchDescriptor md = new MatchDescriptor(sm);
		for (NodeDescriptor n : sm.matchingNewNodes) {
			if (n.tags != null) {
				for (String k : n.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.addNode(n, "New node has watched tag " + k);
					}
				}
			}
		}
		for (NodeDescriptor n : sm.matchingDeletedNodes) {
			if (n.tags != null) {
				for (String k : n.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.addNode(n, "Deleted node has watched tag " + k);
					}
				}
			}
		}
		for (NodeChange n : sm.matchingChangedNodes) {
			if (n.after.tags != null) {
				for (String k : n.after.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.addNode(n.after, "Changed node has watched tag AFTER " + k);
					}
				}
			}
			if (n.before.tags != null) {
				for (String k : n.after.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.addNode(n.before, "Changed node has watched tag BEFORE " + k);
					}
				}
			}
		}
		
		
		for (WayDescriptor n : sm.matchingNewWays) {
			if (n.tags != null) {
				for (String k : n.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.addWay(n, "New way has watched tag " + k);
					}
				}
			}
		}
		for (WayDescriptor n : sm.matchingDeletedWays) {
			if (n.tags != null) {
				for (String k : n.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.addWay(n, "Deleted way has watched tag " + k);
					}
				}
			}
		}
		for (WayDescriptor n : sm.matchingWaysWithChangedNodes) {
			if (n.tags != null) {
				for (String k : n.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.addWay(n, "Way with changed nodes has watched tag " + k);
					}
				}
			}
		}
		for (WayChange n : sm.matchingChangedWays) {
			if (n.after.tags != null) {
				for (String k : n.after.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.addWay(n.after, "Changed way has watched tag AFTER " + k);
					}
				}
			}
			if (n.after.tags != null) {
				for (String k : n.after.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.addWay(n.before, "Changed way has watched tag BEFORE " + k);
					}
				}
			}
		}
		return md;
	}
}
