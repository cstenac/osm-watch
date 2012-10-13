package fr.openstreetmap.watch.matching.misc;

import org.apache.log4j.Logger;

import fr.openstreetmap.watch.matching.Filter;
import fr.openstreetmap.watch.matching.MatchDescriptor;
import fr.openstreetmap.watch.matching.SpatialMatch;
import fr.openstreetmap.watch.model.WayDescriptor;


/**
 * Detects imports that look like french cadastre building imports. 
 */
public class FrenchCadastreImportFilter extends Filter {
	public FrenchCadastreImportFilter(String params) {}
	
	@Override
	public MatchDescriptor matches(SpatialMatch changeset) {
		MatchDescriptor md = new MatchDescriptor(changeset);
		int nbCadastreBuildings = 0;
		int nbBuildings = 0;
		
		for (WayDescriptor wd : changeset.newWays) {
			if (wd.hasTag("building")) {
				nbBuildings++;
				String source = wd.getSourceTag();
				logger.info("Source " + source);
				if (source != null && (source.contains("cadastre-dgi") || source.contains("Cadastre. Mise "))){
					nbCadastreBuildings++;
				}
			}
		}
		
		logger.info("newWays=" + changeset.newWays.size() +  " buildings=" + nbBuildings +  " cadastre=" + nbCadastreBuildings);
		
		if (nbCadastreBuildings > 40) {
			md.matches = true;
			md.reasons.add("More than 40 buildings with Cadastre source");
		}
		return md;
	}
	private static Logger logger = Logger.getLogger("osm");
}
