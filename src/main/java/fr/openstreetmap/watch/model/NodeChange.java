package fr.openstreetmap.watch.model;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


public class NodeChange {
    private static GeometryFactory factory = new GeometryFactory();
    
    public NodeChange( NodeDescriptor before, NodeDescriptor after) {
        Preconditions.checkArgument(before.id == after.id);
        assert(after.changeset > before.changeset);
        assert(after.version > before.version);
        this.id = after.id;
        this.changeset = after.changeset;
        
        this.before = before;
        this.after = after;
    }
    
    public long id;
    
    public long changeset;
    
    public NodeDescriptor before;
    public NodeDescriptor after;
    
    public Point getPointBefore() {
        return factory.createPoint(new Coordinate(before.lon, before.lat));
    }
    public Point getPointAfter() {
        return factory.createPoint(new Coordinate(after.lon, after.lat));
    }
    
    public Set<String> getRemovedTags() {
        return null;
    }
}
