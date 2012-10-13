package fr.openstreetmap.watch.matching;

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import fr.openstreetmap.watch.model.ChangesetDescriptor;
import fr.openstreetmap.watch.model.NodeDescriptor;

public class SpatialFilterTest {
    private void addNewNode(ChangesetDescriptor cd, double lat, double lon, long id) {
        NodeDescriptor n1 = new NodeDescriptor();
        n1.lat = lat;
        n1.lon = lon;
        n1.id = id;
        cd.newNodes.put(id, n1);
    }
    
    public MatchableAlert newBboxAlert(long id, double lat1, double lon1, double lat2, double lon2) {
        GeometryFactory gf = new GeometryFactory();
        MatchableAlert a = new MatchableAlert();
        Envelope e = new Envelope(new Coordinate(lon1, lat1), new Coordinate(lon2, lat2));
        a.polygon = (Polygon)gf.toGeometry(e);
        return a;
    }
    
    @Test
    public void testA() {
        ChangesetDescriptor cd = new ChangesetDescriptor();
        addNewNode(cd, 48, 5, 1000);
        
        SpatialFilter sf = new SpatialFilter();
        sf.addAlert(newBboxAlert(1, 42, 2, 50, 5));
        
        Collection<SpatialMatch> sm = sf.getMatches(cd);
        assertTrue(sm.size() == 1);
        
        SpatialFilter sf2 = new SpatialFilter();
        sf2.addAlert(newBboxAlert(1, 49, 2, 50, 5));
        
        Collection<SpatialMatch> sm2 = sf2.getMatches(cd);
        assertTrue(sm2.size() == 0);
    }
}
