package fr.openstreetmap.watch.matching;

/**
 * Second-level filter, applied after the spatial filter.
 * It returns the final descriptor of the match, if the changeset matches the alert.
 * 
 * Custom filters implement this interface
 */
public abstract class Filter {
	public abstract MatchDescriptor matches(SpatialMatch changeset);
}
