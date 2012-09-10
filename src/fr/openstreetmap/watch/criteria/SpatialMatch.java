package fr.openstreetmap.watch.criteria;

import java.util.ArrayList;
import java.util.List;

import fr.openstreetmap.watch.Alert;
import fr.openstreetmap.watch.model.NodeChange;
import fr.openstreetmap.watch.model.NodeDescriptor;

public class SpatialMatch {
    Alert alert;
    
    List<NodeChange> matchingChangedNodes = new ArrayList<NodeChange>();
    List<NodeDescriptor> matchingNewNodes = new ArrayList<NodeDescriptor>();

}
