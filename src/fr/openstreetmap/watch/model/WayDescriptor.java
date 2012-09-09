package fr.openstreetmap.watch.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class WayDescriptor {
    public long id;
    public long version;
    public long timestamp;
    public long changeset;
    public long uid;
    
    public List<Long> nodes = new ArrayList<Long>();
    
    public Map<String, String> tags;
}
