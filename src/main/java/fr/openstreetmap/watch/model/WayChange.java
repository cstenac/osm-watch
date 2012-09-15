package fr.openstreetmap.watch.model;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;


public class WayChange {
    public WayChange( WayDescriptor before, WayDescriptor after) {
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
    
    public WayDescriptor before;
    public WayDescriptor after;
    
    /* Get the distance in kilometers by which this node moved */
    public double getDisplacement() {
        return 0.0;
    }
    
    public Set<String> getRemovedTags() {
        return null;
    }
}
