package fr.openstreetmap.watch.model;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class NodeDescriptor extends OsmPrimitive {
    private static GeometryFactory factory = new GeometryFactory();
    
    public double lat;
    public double lon;
    
    public Point getPoint() {
        return factory.createPoint(new Coordinate(lon, lat));
    }
}