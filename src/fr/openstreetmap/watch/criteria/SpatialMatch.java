package fr.openstreetmap.watch.criteria;

import java.util.ArrayList;
import java.util.List;

import fr.openstreetmap.watch.Alert;
import fr.openstreetmap.watch.model.NodeChange;
import fr.openstreetmap.watch.model.NodeDescriptor;

public class SpatialMatch {
    public Alert alert;
    
    public List<NodeChange> matchingChangedNodes = new ArrayList<NodeChange>();
    public List<NodeDescriptor> matchingNewNodes = new ArrayList<NodeDescriptor>();

}
