package fr.openstreetmap.watch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vividsolutions.jts.io.ParseException;

import fr.openstreetmap.watch.matching.MatchDescriptor;
import fr.openstreetmap.watch.matching.RuntimeAlert;
import fr.openstreetmap.watch.matching.SpatialFilter;
import fr.openstreetmap.watch.matching.SpatialMatch;
import fr.openstreetmap.watch.model.ChangesetDescriptor;
import fr.openstreetmap.watch.model.db.Alert;
import fr.openstreetmap.watch.model.db.AlertMatch;
import fr.openstreetmap.watch.parsers.AugmentedDiffParser;

@Service
public class Engine {
	private SpatialFilter spatialFilter = new SpatialFilter();
	private DatabaseManager dbManager;
	@Autowired
	public void setDatabaseManager(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	@PostConstruct
	public void init() throws ParseException {
		logger.info("Loading spatial filter");
		/* Preload the filters */
		for (Alert ad : dbManager.getAlerts()) {
			try {
				RuntimeAlert a = new RuntimeAlert(ad);
				spatialFilter.addAlert(a);
			} catch (Exception e) {
				logger.error("Failed to load alert " + ad.getId(), e);
			}
		}
	}

	public void addAlertToSpatialFilter(Alert ad) throws Exception {
		RuntimeAlert a = new RuntimeAlert(ad);
//		dbManager.addAlert(ad);
		spatialFilter.addAlert(a);
	}

	public void handleAugmentedDiff(String contents) throws Exception {
		logger.info("Handling augmented diff file " + contents.length() + " chars");
		AugmentedDiffParser parser = new AugmentedDiffParser();
		parser.parse(contents);

		Map<Long, ChangesetDescriptor> changesets = parser.getChangesets();
		List<ChangesetDescriptor> slist = new ArrayList<ChangesetDescriptor>();
		slist.addAll(changesets.values());
		Collections.sort(slist, new Comparator<ChangesetDescriptor>() {
			@Override
			public int compare(ChangesetDescriptor o1, ChangesetDescriptor o2) {
				return (int)(o1.id - o2.id);
			}
		});
		logger.info("Parsed " + changesets.size() + " changesets");

		dbManager.begin();
		try {
			for (ChangesetDescriptor changeset : slist) {
				Collection<SpatialMatch> spatialMatches = spatialFilter.getMatches(changeset);
				logger.info("Matched changeset " + changeset.id  +", " + spatialMatches.size() + " alerts match");

				for (SpatialMatch sm : spatialMatches) {
					sm.cd = changeset;
					if (sm.alert.josmFilter == null) {
						MatchDescriptor md = new MatchDescriptor(sm);
						md.reasons.add("No tags filtering");
						logger.info("   Tags unfiltered");
						emitMatch(md);
					} else {
						MatchDescriptor md = sm.alert.josmFilter.matches(sm);
						if (md.matches) {
							logger.info("   Tags criterion also matches");
							emitMatch(md);
						} else {
							logger.info("   Tags criterion does not match");
						}
					}
				}
			} 
			dbManager.commit();
		} catch (Exception e) {
			dbManager.rollback();
			throw e;
		}
	}

	private void emitMatch(MatchDescriptor md) {
		logger.info("Recordig match of alert " + md.sm.alert.desc.getId() + " by changeset " + md.sm.cd.id);
		AlertMatch am = new AlertMatch();
		am.setAlert(md.sm.alert.desc);
		am.setMatchTimestamp(System.currentTimeMillis());
		am.setChangesetId(md.sm.cd.id);
		StringBuilder sb = new StringBuilder();
		sb.append("<p>Matches because</p><ul>");
		for (String reason: md.reasons) {
			sb.append("<li>" + reason + "</li>");
		}
		sb.append("</ul>");
		am.setReason(sb.toString());
		dbManager.getEM().persist(am);
	}

	private static Logger logger = Logger.getLogger("osm.watch.engine");
}
