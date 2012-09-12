package fr.openstreetmap.watch.criteria;

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

import fr.openstreetmap.watch.Alert;
import fr.openstreetmap.watch.model.ChangesetDescriptor;
import fr.openstreetmap.watch.model.NodeChange;
import fr.openstreetmap.watch.model.NodeDescriptor;

public class SpatialFilter {
    Quadtree bboxTree = new Quadtree();
    List<Alert> unfilteredAlerts = new ArrayList<Alert>();
    GeometryFactory factory;
    ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public void addAlert(Alert alert) {
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

    private static SpatialMatch getSpatialMatch(Alert a, Map<Long, SpatialMatch> map) {
        if (map.containsKey(a.id)) {
            return map.get(a.id);
        } else {
            SpatialMatch sm = new SpatialMatch();
            sm.alert = a;
            map.put(a.id, sm);
            return sm;
        }
    }

    /**
     * Get the list of alerts that match this changeset on the spatial criteria.
     */
    public Collection<SpatialMatch> getMatches(ChangesetDescriptor changeset) {
        logger.info("Checking match against " + bboxTree.size() + " in tree and " + unfilteredAlerts.size() + " unfiltered");
        rwLock.readLock().lock();
        try {
            Map<Long, SpatialMatch> ret = new HashMap<Long, SpatialMatch>();

            for (NodeChange nd : changeset.changedNodes.values()) {
                Point p = nd.getPointAfter();
                Geometry geom = p.buffer(0.1);
                for (Object o : bboxTree.query(geom.getEnvelopeInternal())) {
                    Alert a = (Alert)o;
                    if (geom.intersects(a.polygonFilter)) {
                        getSpatialMatch(a, ret).matchingChangedNodes.add(nd);
                    }
                }
                for (Alert a : unfilteredAlerts) {
                    getSpatialMatch(a, ret).matchingChangedNodes.add(nd);
                }
            }
            for (NodeDescriptor nd : changeset.newNodes.values()) {
                Geometry geom = nd.getPoint().buffer(0.1);
                for (Object o : bboxTree.query(geom.getEnvelopeInternal())) {
                    Alert a = (Alert)o;
                    if (geom.intersects(a.polygonFilter)) {
                        getSpatialMatch(a, ret).matchingNewNodes.add(nd);
                    }
                }
                for (Alert a : unfilteredAlerts) {
                    getSpatialMatch(a, ret).matchingNewNodes.add(nd);
                }
            }

            return ret.values();

        } finally {
            rwLock.readLock().unlock();
        }
    }
    
    private static Logger logger =Logger.getLogger("osm.watch.filter");

}