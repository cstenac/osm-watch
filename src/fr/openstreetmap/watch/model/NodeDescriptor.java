package fr.openstreetmap.watch.model;
import java.util.Map;


public class NodeDescriptor {
    public long id;
    public double lat;
    public double lon;
    public long version;
    public long timestamp;
    
    public long changeset;
    
    public long uid;
    
    public Map<String, String> tags;
}
