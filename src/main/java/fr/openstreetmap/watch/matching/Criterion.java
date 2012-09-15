package fr.openstreetmap.watch.matching;


public abstract class Criterion {
	public abstract MatchDescriptor matches(SpatialMatch changeset);
}
