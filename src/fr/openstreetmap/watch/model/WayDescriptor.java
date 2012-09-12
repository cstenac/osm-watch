package fr.openstreetmap.watch.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.LineString;


public class WayDescriptor {
    public long id;
    public long version;
    public long timestamp;
    public long changeset;
    public long uid;
    
    public List<Long> nodes = new ArrayList<Long>();
    public LineString line;
    
    public Map<String, String> tags;
}
