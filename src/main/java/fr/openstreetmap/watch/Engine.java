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
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;

import com.vividsolutions.jts.io.ParseException;

import fr.openstreetmap.watch.matching.MatchDescriptor;
import fr.openstreetmap.watch.matching.MatchableAlert;
import fr.openstreetmap.watch.matching.SpatialFilter;
import fr.openstreetmap.watch.matching.SpatialMatch;
import fr.openstreetmap.watch.model.ChangesetDescriptor;
import fr.openstreetmap.watch.model.db.Alert;
import fr.openstreetmap.watch.model.db.AlertMatch;
import fr.openstreetmap.watch.parsers.AugmentedDiffParser;
import fr.openstreetmap.watch.parsers.AugmentedDiffV2Parser;
import fr.openstreetmap.watch.parsers.DiffParser;

@Service
public class Engine {
	private SpatialFilter spatialFilter = new SpatialFilter();
	private DatabaseManager dbManager;
	@Autowired
	public void setDatabaseManager(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	@PostConstruct
	public void pc() throws ParseException {
		logger.info("Loading spatial filter");
		dbManager.begin();
		/* Preload the filters */
		for (Alert ad : dbManager.getAlerts()) {
			try {
				addAlertToSpatialFilter(ad);
			} catch (Exception e) {
				logger.error("Failed to load alert " + ad.getId(), e);
			}
		}
		dbManager.rollback();
	}
	
	private @Autowired AutowireCapableBeanFactory beanFactory;

	public synchronized void addAlertToSpatialFilter(Alert ad) throws Exception {
		MatchableAlert a = new MatchableAlert(ad);
		
		/* The filter can have some autowired dependencies, let's give them */
		if (a.getFilter() != null) {
			beanFactory.autowireBean(a.getFilter());
		}
		spatialFilter.addAlert(a);
	}
	
	public synchronized void rebuildSpatialFilter() throws Exception {
		spatialFilter.clear();
		dbManager.begin();
		/* Preload the filters */
		for (Alert ad : dbManager.getAlerts()) {
			try {
				addAlertToSpatialFilter(ad);
			} catch (Exception e) {
				logger.error("Failed to load alert " + ad.getId(), e);
			}
		}
		dbManager.rollback();
	}

	public synchronized void handleAugmentedDiff(String contents, int version) throws Exception {
		logger.info("Handling augmented diff file " + contents.length() + " chars");
		long before = System.currentTimeMillis();
		DiffParser parser = null;
		if (version == 1) {
		    parser = new AugmentedDiffParser();
		    parser.parse(contents);
		} else {
            parser = new AugmentedDiffV2Parser();
            parser.parse(contents);
		}

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
		
		State.parsingProcessingTime += (System.currentTimeMillis() - before);
		
		dbManager.begin();
		try {
			for (ChangesetDescriptor changeset : slist) {
				int matches = 0;
				State.processedChangesets++;
				logger.info("Handling changeset " + changeset.id);
				changeset.computeBBox();
				changeset.computeUser();

				if (dbManager.getEM().createQuery("Select x from Changeset x where x.id=" + changeset.id).getResultList().size() == 0) {
					dbManager.getEM().persist(changeset.toDBModel());
				} else {
					logger.info("Need to update changeset ... TODO " + changeset.id);
				}
				
//			    System.out.println("\n" + ChangesetDescriptorDumper.dump(changeset) );
				Collection<SpatialMatch> spatialMatches = spatialFilter.getMatches(changeset);

				for (SpatialMatch sm : spatialMatches) {
                    logger.info("Changeset " + changeset.id + " geomatches alert " + sm.alert.desc.getId());
					sm.cd = changeset;
					if (sm.alert.getFilter() == null) {
						MatchDescriptor md = new MatchDescriptor(sm);
						md.reasons.add("No filter");
						logger.info("   No filter -> matches");
						matches++;
						emitMatch(md);
					} else {
						MatchDescriptor md = sm.alert.getFilter().matches(sm);
						if (md.matches) {
							logger.info("   Filter matches(" + sm.alert.desc.getFilterClass() + " - " + sm.alert.desc.getFilterParams() + ")");
							matches++;
							emitMatch(md);
						} else {
							logger.info("   Filter does not match (" + sm.alert.desc.getFilterClass() + " - " + sm.alert.desc.getFilterParams() + ")");
						}
					}
				}
				if (matches > 0) State.matchedChangesets++;
			} 
			dbManager.commit();
		} catch (Exception e) {
			dbManager.rollback();
			throw e;
		}
	}

	private void emitMatch(MatchDescriptor md) {
		State.emittedMatches ++;
		logger.info("Recording match of alert " + md.getSpatialMatch().alert.desc.getId() + " by changeset " + md.getSpatialMatch().cd.id);
		AlertMatch am = new AlertMatch();
		am.setAlert(md.getSpatialMatch().alert.desc);
		am.setMatchTimestamp(System.currentTimeMillis());
		am.setChangesetId(md.getSpatialMatch().cd.id);
		am.setChangesetUserName(md.getSpatialMatch().cd.user);
		am.setMinX(md.minX);
		am.setMaxX(md.maxX);
		am.setMinY(md.minY);
		am.setMaxY(md.maxY);
		
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
