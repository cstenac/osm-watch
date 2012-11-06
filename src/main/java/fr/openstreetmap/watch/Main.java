package fr.openstreetmap.watch;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;

import fr.openstreetmap.watch.model.ChangesetDescriptor;
import fr.openstreetmap.watch.parsers.AugmentedDiffParser;
import fr.openstreetmap.watch.parsers.AugmentedDiffV2Parser;


public class Main {
    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        String s = FileUtils.readFileToString(new File("/tmp/000.osc"), "utf8");
        s = s.replace("&", "&amp;");
        AugmentedDiffV2Parser opf = new AugmentedDiffV2Parser();
        opf.parse(s);
        
        /*
        System.out.println(opf.changedNodes.size());
        for (NodeChange nc : opf.changedNodes.values()) {
            System.out.println(" CHANGED IN " + nc.changeset + " from " + nc.before.version +" to " + nc.after.version + " from c" + nc.before.changeset);
        }
        System.out.println(opf.reallyDeletedNodes.size());
        for (NodeDescriptor nd : opf.reallyDeletedNodes.values()) {
            System.out.println(" DELETED IN " + nd.changeset +" -> " + nd.id);
        }
        System.out.println(opf.newNodes.size());
        
        System.out.println(opf.changedWays.size());
        System.out.println(opf.waysWithChangedNodes.size());
        System.out.println(opf.reallyDeletedWays.size());
        System.out.println(opf.newWays.size());
        */
        
        List<ChangesetDescriptor> l = new ArrayList<ChangesetDescriptor>();
        l.addAll(opf.getChangesets().values());
        Collections.sort(l, new Comparator<ChangesetDescriptor>() {
            @Override
            public int compare(ChangesetDescriptor arg0, ChangesetDescriptor arg1) {
                return (int)(arg0.id - arg1.id);
            }
        });
        
        for (ChangesetDescriptor cd : l) {
            System.out.println(ChangesetDescriptorDumper.dump(cd));
        }
    }
}
