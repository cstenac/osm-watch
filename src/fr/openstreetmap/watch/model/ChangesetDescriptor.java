package fr.openstreetmap.watch.model;
import java.util.HashMap;
import java.util.Map;


public class ChangesetDescriptor {
	public long id;
    
    public Map<Long, NodeDescriptor> deletedNodes = new HashMap<Long, NodeDescriptor>();
    public Map<Long, NodeChange> changedNodes = new HashMap<Long, NodeChange>();
    public Map<Long, NodeDescriptor> newNodes = new HashMap<Long, NodeDescriptor>();
    
    public Map<Long, WayDescriptor> waysWithChangedNodes = new HashMap<Long, WayDescriptor>();// TODO DODO 
    public Map<Long, WayDescriptor> deletedWays = new HashMap<Long, WayDescriptor>();
    public Map<Long, WayChange> changedWays = new HashMap<Long, WayChange>(); 
    public Map<Long, WayDescriptor> newWays = new HashMap<Long, WayDescriptor>();
}
