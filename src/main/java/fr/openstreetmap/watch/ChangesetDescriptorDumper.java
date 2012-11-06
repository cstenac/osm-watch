package fr.openstreetmap.watch;

import com.vividsolutions.jts.io.WKTWriter;

import fr.openstreetmap.watch.model.ChangesetDescriptor;
import fr.openstreetmap.watch.model.NodeChange;
import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.WayChange;
import fr.openstreetmap.watch.model.WayDescriptor;

public class ChangesetDescriptorDumper {
    public static String dump(ChangesetDescriptor cd) {
        StringBuilder sb = new StringBuilder();
        cd.computeUser();
      sb.append("<Changeset " + cd.id + " u=" + cd.user + ">\n");
        for (NodeChange nc : cd.changedNodes.values()) {
            sb.append("  <ChangedNode id=" + nc.id + " fromVersion=" + nc.before.version +" toVersion=" + nc.after.version + " fromChange=" + nc.before.changeset + " u=" + nc.after.getUser() + "/>\n");
        }
        for (NodeDescriptor nd : cd.deletedNodes.values()) {
            sb.append("  <DeletedNode id=" + nd.id + " u=" + nd.getUser() + "/>\n");
        }
        for (NodeDescriptor nd : cd.newNodes.values()) {
            sb.append("  <NewNode id=" + nd.id + " u=" + nd.getUser() +" />\n");
        }
        
        for (WayChange nc : cd.changedWays.values()) {
            sb.append("  <ChangedWay id=" + nc.id + " fromVersion=" + nc.before.version +" toVersion=" + nc.after.version + " fromChange=" + nc.before.changeset);
            if (nc.after.line != null) {
                sb.append(" line=" + new WKTWriter().write(nc.after.line));
            }
            sb.append("u=" + nc.after.getUser() + " />\n");
        }
        for (WayDescriptor nd : cd.deletedWays.values()) {
            sb.append("  <DeletedWay id=" + nd.id + " nodes=" + nd.nodes.size());
            if (nd.line != null) {
                sb.append(" line=" + new WKTWriter().write(nd.line));
            }
            sb.append("u=" + nd.getUser() + " />\n");
        }
        for (WayDescriptor nd : cd.newWays.values()) {
            sb.append("  <NewWay id=" + nd.id + " nodes=" + nd.nodes.size());
            if (nd.line != null) {
                sb.append(" line=" + new WKTWriter().write(nd.line));
            }
            sb.append("u=" + nd.getUser() + " />\n");

        }
        sb.append("</Changeset>");

        return sb.toString();
    }
    
}
