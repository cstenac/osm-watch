package fr.openstreetmap.watch.criteria;

import java.util.List;

import com.vividsolutions.jts.index.quadtree.Quadtree;

import fr.openstreetmap.watch.Alert;
import fr.openstreetmap.watch.model.ChangesetDescriptor;

public class SpatialFilter {
	Quadtree boundingBoxTree;
	Quadtree polygonTree;
	
	public void addAlert(Alert alert) {
	}
	
	/**
	 * Get the list of alerts that match this changeset on the spatial criteria.
	 */
	public List<Alert> getMatches(ChangesetDescriptor changeset) {
		return null;
	}
	
}
