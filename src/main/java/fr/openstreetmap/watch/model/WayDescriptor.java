package fr.openstreetmap.watch.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import com.vividsolutions.jts.geom.LineString;


public class WayDescriptor extends OsmPrimitive {
    public List<Long> nodes = new ArrayList<Long>();
    public LineString line;
    
    public boolean isClosed() {
        if (line == null) {
            return false;
        } else {
            return line.isClosed();
        }
    }
}
