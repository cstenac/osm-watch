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

import fr.openstreetmap.watch.criteria.SpatialFilter;
import fr.openstreetmap.watch.criteria.SpatialMatch;
import fr.openstreetmap.watch.model.AlertDesc;
import fr.openstreetmap.watch.model.ChangesetDescriptor;
import fr.openstreetmap.watch.model.MatchDescriptor;
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
        for (AlertDesc ad : dbManager.getAlerts()) {
            try {
            Alert a = new Alert(ad);
            spatialFilter.addAlert(a);
            } catch (Exception e) {
                logger.error("Failed to load alert " + ad.getId(), e);
            }
        }
    }

    public void addAlert(AlertDesc ad) throws Exception {
        Alert a = new Alert(ad);
        dbManager.addAlert(ad);
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

        for (ChangesetDescriptor changeset : slist) {
            Collection<SpatialMatch> spatialMatches = spatialFilter.getMatches(changeset);
            logger.info("Matched changeset " + changeset.id  +", " + spatialMatches.size() + " alerts match");

            for (SpatialMatch sm : spatialMatches) {
                if (sm.alert.tagsFilter == null) {
                    emitMatch(sm.alert);
                } else {
                    MatchDescriptor md = sm.alert.tagsFilter.matches(changeset);
                    if (md.matches) {
                        logger.info("Tags criterion also matches");
                        emitMatch(sm.alert);
                    } else {
                        logger.info("Tags criterion does not match");
                    }
                }
            }
        }
    }

    private void emitMatch(Alert a) {

    }

    private static Logger logger = Logger.getLogger("osm.watch.engine");
}
