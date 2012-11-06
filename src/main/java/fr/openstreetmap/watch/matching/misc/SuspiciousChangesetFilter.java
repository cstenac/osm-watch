package fr.openstreetmap.watch.matching.misc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import fr.openstreetmap.watch.DatabaseManager;
import fr.openstreetmap.watch.matching.Filter;
import fr.openstreetmap.watch.matching.MatchDescriptor;
import fr.openstreetmap.watch.matching.SpatialMatch;
import fr.openstreetmap.watch.model.WayChange;


/**
 * Detects changesets that might be vandalism or error:
 *   - More than 200 objets deleted in a changeset
 *   - A way has moved by ~ more than 200 meters
 */
public class SuspiciousChangesetFilter extends Filter {
//    Match suspiciousSources;

    int maxDeletes = 200;

    public SuspiciousChangesetFilter(String params) {
//        try {
//            suspiciousSources = SearchCompiler.compile("source=\".*oogle.*\"", false, true);
//        } catch (ParseError e) {
//            throw new Error("The impossible happened", e);
//        }
    }

    @Autowired public DatabaseManager dbManager;

    @Override
    public MatchDescriptor matches(SpatialMatch changeset) {
        MatchDescriptor md = new MatchDescriptor(changeset);

        Session hibernateSession = dbManager.getEM().unwrap(Session.class);
        @SuppressWarnings("deprecation")
        Connection connection = hibernateSession.connection(); 

        Statement st = null;
        try {
            st = connection.createStatement();
            for (WayChange wc : changeset.changedWays) {
                double lengthAfter = wc.after.line.getLength();
                /* Only look at ways "long enough", and for which the length did not change too dramatically, because
                 * we don't want to yell on ways split or extended
                 */
                // 0.001 degree = between 19 and 111 meters
                if (lengthAfter < 0.001) {
                    //                    logger.info("Not computing length, too short");
                    continue;
                }
                double lengthBefore = wc.before.line.getLength();
                if (lengthAfter / lengthBefore > 2 || lengthAfter  / lengthBefore < 0.5) continue;

                //                logger.info("Computing Maxdistance for " + wc.id);

                st.execute("SELECT ST_HausdorffDistance(ST_GeomFromEWKT('SRID=4326;" + wc.before.line.toText() +
                        "'), ST_GeomFromEWKT('SRID=4326;" + wc.after.line.toText()  + "'))");
                ResultSet rs  = st.getResultSet();
                rs.next();
                double distance = rs.getDouble(1);

                //                logger.info(" ************** " + distance);

                // 1 degree = 111km at equator, 19km at 80° latitude 
                if (distance > 0.002) {
                    md.addWay(wc.after, "Way has moved quite a lot (" + distance + " degrees)");
                    //                    logger.info("BEFORE " + wc.before.line.toText());
                    //                    logger.info("AFTER  " + wc.after.line.toText());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get distance", e);
        } finally {
            try { st.close(); } catch (Exception e) { logger.error("Failed to close statement", e);}
        }

        int nbDeletions = changeset.deletedNodes.size() + changeset.deletedWays.size();

        if (nbDeletions >  maxDeletes) {
            md.setMatchBboxAsChangesetBbox();
            md.matches = true;
            md.reasons.add("" + nbDeletions + " objects were deleted");
        }

        return md;
    }
    private static Logger logger = Logger.getLogger("osm");
}
