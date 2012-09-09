package fr.openstreetmap.watch.criteria;

import fr.openstreetmap.watch.model.ChangesetDescriptor;
import fr.openstreetmap.watch.model.MatchDescriptor;

public abstract class Criterion {
	public abstract MatchDescriptor matches(ChangesetDescriptor changeset);
}
