package fr.openstreetmap.watch.matching;

import java.util.Set;

import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.WayChange;
import fr.openstreetmap.watch.model.WayDescriptor;

public class TagKeysCriterion extends Criterion {
	Set<String> watchedKeys;
	public TagKeysCriterion(Set<String> watchedKeys) {
		this.watchedKeys = watchedKeys;
	}
	@Override
	public MatchDescriptor matches(SpatialMatch changeset) {
		MatchDescriptor md = new MatchDescriptor();
		for (NodeDescriptor n : changeset.matchingNewNodes) {
			if (n.tags != null) {
				for (String k : n.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.addNode(n, "New node has watched tag " + k);
					}
				}
			}
		}
		for (WayDescriptor n : changeset.matchingNewWays) {
			if (n.tags != null) {
				for (String k : n.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.matches = true;
						//(n, "New way has watched tag " + k);
					}
				}
			}
		}
		for (WayChange n : changeset.matchingChangedWays) {
			if (n.after.tags != null) {
				for (String k : n.after.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.matches = true;
						//(n, "New way has watched tag " + k);
					}
				}
			}
		}
		return md;
	}
}
