import java.util.HashMap;
import java.util.Map;


public class ChangesetDescriptor {
    long id;
    
    Map<Long, NodeDescriptor> deletedNodes = new HashMap<Long, NodeDescriptor>();
    Map<Long, NodeChange> changedNodes = new HashMap<Long, NodeChange>();
    Map<Long, NodeDescriptor> newNodes = new HashMap<Long, NodeDescriptor>();
    
    Map<Long, WayDescriptor> waysWithChangedNodes = new HashMap<Long, WayDescriptor>();// TODO DODO 
    Map<Long, WayDescriptor> deletedWays = new HashMap<Long, WayDescriptor>();
    Map<Long, WayChange> changedWays = new HashMap<Long, WayChange>(); 
    Map<Long, WayDescriptor> newWays = new HashMap<Long, WayDescriptor>();
}
