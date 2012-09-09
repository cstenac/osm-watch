package fr.openstreetmap.watch.model;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;


public class NodeChange {
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
    
    /* Get the distance in kilometers by which this node moved */
    public double getDisplacement() {
        return 0.0;
    }
    
    public Set<String> getRemovedTags() {
        return null;
    }
}
