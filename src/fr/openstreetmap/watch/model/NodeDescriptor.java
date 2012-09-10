package fr.openstreetmap.watch.model;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


public class NodeDescriptor {
    private static GeometryFactory factory = new GeometryFactory();
    
    public long id;
    public double lat;
    public double lon;
    public long version;
    public long timestamp;
    
    public long changeset;
    
    public long uid;
    
    public Point getPoint() {
        return factory.createPoint(new Coordinate(lon, lat));
    }
    
    public Map<String, String> tags;
}