package fr.openstreetmap.watch.matching.misc;

import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import fr.openstreetmap.watch.DatabaseManager;
import fr.openstreetmap.watch.matching.Filter;
import fr.openstreetmap.watch.matching.MatchDescriptor;
import fr.openstreetmap.watch.matching.SpatialMatch;


/**
 * Detects when we see a new contributor for the first time. 
 */
public class FirstTimeContributorFilter extends Filter {
	public FirstTimeContributorFilter(String params) {}
	
	private @Autowired DatabaseManager dbManager;
	
	@Override
	public MatchDescriptor matches(SpatialMatch changeset) {
		MatchDescriptor md = new MatchDescriptor(changeset);
		
		Query q = dbManager.getEM().createQuery("SELECT x FROM Changeset x where uid = ?1 AND id != ?2");
		q.setParameter(1, changeset.cd.uid);
		q.setParameter(2, changeset.cd.id);
		
		if (q.getResultList().size() == 0) {
			logger.info("No previous contribution from " + changeset.cd.user);
			md.matches = true;
			md.setMatchBboxAsChangesetBbox();
			md.reasons.add("First contribution for " + changeset.cd.user);
		} else {
//			logger.info("Already seen " + q.getResultList().size() + " contribs from " + changeset.cd.user);
		}
		return md;
	}
	private static Logger logger = Logger.getLogger("osm");
}