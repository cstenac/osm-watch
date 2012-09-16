package fr.openstreetmap.watch.matching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.quadtree.Quadtree;

import fr.openstreetmap.watch.model.ChangesetDescriptor;
import fr.openstreetmap.watch.model.NodeChange;
import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.WayChange;
import fr.openstreetmap.watch.model.WayDescriptor;

public class SpatialFilter {
    Quadtree bboxTree = new Quadtree();
    List<RuntimeAlert> unfilteredAlerts = new ArrayList<RuntimeAlert>();
    GeometryFactory factory;
    ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public void addAlert(RuntimeAlert alert) {
        rwLock.writeLock().lock();
        try {
            if (alert.polygonFilter != null) {
                bboxTree.insert(alert.polygonFilter.getEnvelopeInternal(), alert);
            } else {
                unfilteredAlerts.add(alert);
            }

        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private static SpatialMatch getSpatialMatch(RuntimeAlert a, Map<Long, SpatialMatch> map) {
        if (map.containsKey(a.desc.getId())) {
            return map.get(a.desc.getId());
        } else {
            SpatialMatch sm = new SpatialMatch();
            sm.alert = a;
            map.put(a.desc.getId(), sm);
            return sm;
        }
    }

    /**
     * Get the list of alerts that match this changeset on the spatial criteria.
     */
    public Collection<SpatialMatch> getMatches(ChangesetDescriptor changeset) {
//        logger.info("Checking match against " + bboxTree.size() + " in tree and " + unfilteredAlerts.size() + " unfiltered");
        rwLock.readLock().lock();
        try {
            Map<Long, SpatialMatch> ret = new HashMap<Long, SpatialMatch>();

            /* ***************** Match the nodes **************** */
            for (NodeChange nd : changeset.changedNodes.values()) {
//                logger.info("Checking " + nd.getPointAfter());
                Point p = nd.getPointAfter();
                Geometry geom = p.buffer(0.1);
                for (Object o : bboxTree.query(geom.getEnvelopeInternal())) {
                    RuntimeAlert a = (RuntimeAlert)o;
//                    logger.info("cnode " + p + " intersets box " + a.desc.getId()  + " - compare it to " + a.polygonFilter);
                    if (geom.intersects(a.polygonFilter)) {
//                        logger.info("cnode " + p + " DOES MATCH !!!!! " + a.desc.getId());
                        getSpatialMatch(a, ret).matchingChangedNodes.add(nd);
                    }
                }
                for (RuntimeAlert a : unfilteredAlerts) {
                    getSpatialMatch(a, ret).matchingChangedNodes.add(nd);
                }
            }
            for (NodeDescriptor nd : changeset.newNodes.values()) {
                Geometry geom = nd.getPoint().buffer(0.1);
                for (Object o : bboxTree.query(geom.getEnvelopeInternal())) {
                    RuntimeAlert a = (RuntimeAlert)o;
//                    logger.info("newnode " + geom + " intersets box " + a.id +"  "  + a.polygonFilter);
                    if (geom.intersects(a.polygonFilter)) {
//                        logger.info("INTERSECTS ");
                        getSpatialMatch(a, ret).matchingNewNodes.add(nd);
                    }
                }
                for (RuntimeAlert a : unfilteredAlerts) {
                    getSpatialMatch(a, ret).matchingNewNodes.add(nd);
                }
            }
            for (NodeDescriptor nd : changeset.deletedNodes.values()) {
                Geometry geom = nd.getPoint().buffer(0.1);
                for (Object o : bboxTree.query(geom.getEnvelopeInternal())) {
                    RuntimeAlert a = (RuntimeAlert)o;
                    if (geom.intersects(a.polygonFilter)) {
                        getSpatialMatch(a, ret).matchingDeletedNodes.add(nd);
                    }
                }
                for (RuntimeAlert a : unfilteredAlerts) {
                    getSpatialMatch(a, ret).matchingDeletedNodes.add(nd);
                }
            }

            /* ***************** Match the ways **************** */

            for (WayChange nd : changeset.changedWays.values()) {
                if (nd.after.line == null) {
                	logger.info("Can't match way " + nd.after.id + ", no line");
                	continue;
                }
                
                
                for (Object o : bboxTree.query(nd.after.line.getEnvelopeInternal())) {
                    RuntimeAlert a = (RuntimeAlert)o;
//                    logger.info("cway " + nd.after.line);
                    if (nd.after.line.intersects(a.polygonFilter)) {
//                        logger.info("INTERSECTS " + nd.after.line);
                        getSpatialMatch(a, ret).matchingChangedWays.add(nd);
                    }
                }
                for (RuntimeAlert a : unfilteredAlerts) {
                    getSpatialMatch(a, ret).matchingChangedWays .add(nd);
                }
            }
            for (WayDescriptor nd : changeset.newWays.values()) {
                if (nd.line == null) continue;
                for (Object o : bboxTree.query(nd.line.getEnvelopeInternal())) {
                    RuntimeAlert a = (RuntimeAlert)o;
                    if (nd.line.intersects(a.polygonFilter)) {
                        getSpatialMatch(a, ret).matchingNewWays.add(nd);
                    }
                }
                for (RuntimeAlert a : unfilteredAlerts) {
                    getSpatialMatch(a, ret).matchingNewWays.add(nd);
                }
            }
            for (WayDescriptor nd : changeset.deletedWays.values()) {
                if (nd.line == null) continue;
                for (Object o : bboxTree.query(nd.line.getEnvelopeInternal())) {
                    RuntimeAlert a = (RuntimeAlert)o;
                    if (nd.line.intersects(a.polygonFilter)) {
                        getSpatialMatch(a, ret).matchingDeletedWays.add(nd);
                    }
                }
                for (RuntimeAlert a : unfilteredAlerts) {
                    getSpatialMatch(a, ret).matchingDeletedWays.add(nd);
                }
            }
//            logger.info("Matched: " + ret.size() + " alerts match this changeset");
            return ret.values();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private static Logger logger =Logger.getLogger("osm.watch.filter");

}