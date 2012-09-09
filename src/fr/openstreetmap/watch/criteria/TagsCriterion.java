package fr.openstreetmap.watch.criteria;

import java.util.Set;

import fr.openstreetmap.watch.model.ChangesetDescriptor;
import fr.openstreetmap.watch.model.MatchDescriptor;
import fr.openstreetmap.watch.model.NodeDescriptor;

public class TagsCriterion extends Criterion {
	Set<String> watchedKeys;
	public TagsCriterion(Set<String> watchedKeys) {
		this.watchedKeys = watchedKeys;
	}
	@Override
	public MatchDescriptor matches(ChangesetDescriptor changeset) {
		MatchDescriptor md = new MatchDescriptor();
		for (NodeDescriptor n : changeset.newNodes.values()) {
			if (n.tags != null) {
				for (String k : n.tags.keySet()) {
					if (watchedKeys.contains(k)) {
						md.addNode(n, "New node has watched tag " + k);
					}
				}
			}
		}
		return md;
	}
}
