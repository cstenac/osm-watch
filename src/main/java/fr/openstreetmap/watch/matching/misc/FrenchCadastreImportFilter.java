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
				logger.debug("Source " + source);
				if (source != null && (source.contains("cadastre-dgi") || source.contains("Cadastre. Mise "))){
					nbCadastreBuildings++;
					// We add manually the way to compute the truly matching bbox
					md.addWay(wd, "Cadastre building");
				}
			}
		}
		
		logger.debug("newWays=" + changeset.newWays.size() +  " buildings=" + nbBuildings +  " cadastre=" + nbCadastreBuildings);
		
		if (nbCadastreBuildings > 40) {
			md.matches = true;
			md.reasons.clear();
			md.reasons.add("" + nbCadastreBuildings + " buildings with Cadastre source");
		} else {
			md.matches = false;
			md.reasons.clear();
		}
		return md;
	}
	private static Logger logger = Logger.getLogger("osm");
}
