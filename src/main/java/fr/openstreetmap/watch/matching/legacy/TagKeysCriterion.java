package fr.openstreetmap.watch.matching.legacy;

import java.util.HashSet;
import java.util.Set;

import fr.openstreetmap.watch.matching.Filter;
import fr.openstreetmap.watch.matching.MatchDescriptor;
import fr.openstreetmap.watch.matching.SpatialMatch;
import fr.openstreetmap.watch.model.NodeChange;
import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.WayChange;
import fr.openstreetmap.watch.model.WayDescriptor;

/**
 * Legacy/example filter based on tag keys.
 */
public class TagKeysCriterion extends Filter {
	Set<String> watchedKeys;
	public TagKeysCriterion(String watchedKeys) {
		Set<String> set = new HashSet<String>();
		for (String s : watchedKeys.split(",")) set.add(s);
		this.watchedKeys = set;
	}
	@Override
	public MatchDescriptor matches(SpatialMatch sm) {
		MatchDescriptor md = new MatchDescriptor(sm);
		for (NodeDescriptor n : sm.newNodes) {
			if (n.tags != null) {
				for (String k : n.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.addNode(n, "New node has watched tag " + k);
					}
				}
			}
		}
		for (NodeDescriptor n : sm.deletedNodes) {
			if (n.tags != null) {
				for (String k : n.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.addNode(n, "Deleted node has watched tag " + k);
					}
				}
			}
		}
		for (NodeChange n : sm.changedNodes) {
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


		for (WayDescriptor n : sm.newWays) {
			if (n.tags != null) {
				for (String k : n.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.addWay(n, "New way has watched tag " + k);
					}
				}
			}
		}
		for (WayDescriptor n : sm.deletedWays) {
			if (n.tags != null) {
				for (String k : n.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.addWay(n, "Deleted way has watched tag " + k);
					}
				}
			}
		}
		for (WayDescriptor n : sm.waysWithChangedNodes) {
			if (n.tags != null) {
				for (String k : n.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.addWay(n, "Way with changed nodes has watched tag " + k);
					}
				}
			}
		}
		for (WayChange n : sm.changedWays) {
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
