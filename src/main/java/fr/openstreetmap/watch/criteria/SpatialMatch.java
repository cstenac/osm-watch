package fr.openstreetmap.watch.criteria;

import java.util.ArrayList;
import java.util.List;

import fr.openstreetmap.watch.Alert;
import fr.openstreetmap.watch.model.NodeChange;
import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.WayChange;
import fr.openstreetmap.watch.model.WayDescriptor;

public class SpatialMatch {
    public Alert alert;
    
    public List<NodeChange> matchingChangedNodes = new ArrayList<NodeChange>();
    public List<NodeDescriptor> matchingNewNodes = new ArrayList<NodeDescriptor>();
    public List<NodeDescriptor> matchingDeletedNodes = new ArrayList<NodeDescriptor>();

    public List<WayChange> matchingChangedWays = new ArrayList<WayChange>();
    public List<WayDescriptor> matchingNewWays = new ArrayList<WayDescriptor>();
    public List<WayDescriptor> matchingDeletedWays = new ArrayList<WayDescriptor>();
    public List<WayDescriptor> matchingWaysWithChangedNodes = new ArrayList<WayDescriptor>();
}