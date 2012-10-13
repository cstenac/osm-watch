package fr.openstreetmap.watch.matching;

import java.util.ArrayList;
import java.util.List;

import fr.openstreetmap.watch.model.ChangesetDescriptor;
import fr.openstreetmap.watch.model.NodeChange;
import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.WayChange;
import fr.openstreetmap.watch.model.WayDescriptor;

/**
 * State of a changeset after filtering by the alert's polygon.
 * 
 * It contains the exact subset of changeset elements that matched the spatial filter.
 */
public class SpatialMatch {
    public MatchableAlert alert;
    public ChangesetDescriptor cd;
    
    public List<NodeChange> changedNodes = new ArrayList<NodeChange>();
    public List<NodeDescriptor> newNodes = new ArrayList<NodeDescriptor>();
    public List<NodeDescriptor> deletedNodes = new ArrayList<NodeDescriptor>();

    public List<WayChange> changedWays = new ArrayList<WayChange>();
    public List<WayDescriptor> newWays = new ArrayList<WayDescriptor>();
    public List<WayDescriptor> deletedWays = new ArrayList<WayDescriptor>();
    public List<WayDescriptor> waysWithChangedNodes = new ArrayList<WayDescriptor>();
}