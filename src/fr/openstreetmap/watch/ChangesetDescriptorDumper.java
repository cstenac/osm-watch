package fr.openstreetmap.watch;

import fr.openstreetmap.watch.model.ChangesetDescriptor;
import fr.openstreetmap.watch.model.NodeChange;
import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.WayChange;
import fr.openstreetmap.watch.model.WayDescriptor;

public class ChangesetDescriptorDumper {
    public static String dump(ChangesetDescriptor cd) {
        StringBuilder sb = new StringBuilder();
        
      sb.append("<Changeset " + cd.id + ">\n");
        for (NodeChange nc : cd.changedNodes.values()) {
            sb.append("  <ChangedNode id=" + nc.id + " fromVersion=" + nc.before.version +" toVersion=" + nc.after.version + " fromChange=" + nc.before.changeset + "/>\n");
        }
        for (NodeDescriptor nd : cd.deletedNodes.values()) {
            sb.append("  <DeletedNode id=" + nd.id + "/>\n");
        }
        for (NodeDescriptor nd : cd.newNodes.values()) {
            sb.append("  <NewNode id=" + nd.id + "/>\n");
        }
        
        for (WayChange nc : cd.changedWays.values()) {
            sb.append("  <ChangedWay id=" + nc.id + " fromVersion=" + nc.before.version +" toVersion=" + nc.after.version + " fromChange=" + nc.before.changeset + "/>\n");
        }
        for (WayDescriptor nd : cd.deletedWays.values()) {
            sb.append("  <DeletedWay id=" + nd.id + " nodes=" + nd.nodes.size() + "/> \n");
        }
        for (WayDescriptor nd : cd.newWays.values()) {
            sb.append("  <NewWay id=" + nd.id + " nodes=" + nd.nodes.size() + "/>\n");
        }
        sb.append("</Changeset>");

        return sb.toString();
    }
    
}
