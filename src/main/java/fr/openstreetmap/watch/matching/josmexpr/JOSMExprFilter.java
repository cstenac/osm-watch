package fr.openstreetmap.watch.matching.josmexpr;

import fr.openstreetmap.watch.matching.Filter;
import fr.openstreetmap.watch.matching.MatchDescriptor;
import fr.openstreetmap.watch.matching.SpatialMatch;
import fr.openstreetmap.watch.matching.josmexpr.SearchCompiler.Match;
import fr.openstreetmap.watch.matching.josmexpr.SearchCompiler.ParseError;
import fr.openstreetmap.watch.model.NodeChange;
import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.WayChange;
import fr.openstreetmap.watch.model.WayDescriptor;

/**
 * This filter uses JOSM Search expressions, using the JOSM parser and evaluator.
 * It provides a vast array of conditions on tags.
 */
public class JOSMExprFilter extends Filter {
    Match josmMatch;

    public JOSMExprFilter(String expr) throws ParseError {
        josmMatch = SearchCompiler.compile(expr, false, true);
    }

    @Override
    public MatchDescriptor matches(SpatialMatch sm) {
        MatchDescriptor md = new MatchDescriptor(sm);
        for (NodeDescriptor n : sm.newNodes) {
            if (josmMatch.match(n)) {
                md.addNode(n, "New node matches expression");
            }
        }
        for (NodeDescriptor n : sm.deletedNodes) {
            if (josmMatch.match(n)) {
                md.addNode(n, "Deleted node matches expression");
            }
        }
        for (NodeChange n : sm.changedNodes) {
            boolean after = josmMatch.match(n.after);
            boolean before = josmMatch.match(n.before);
            if (before && after) {
                md.addNode(n.after, "Changed node matches expression before and after change");
            } else if (before) {
                md.addNode(n.before, "Changed node matched expression before change");
            } else if (after) {
                md.addNode(n.after, "Changed node matches expression after change");
            }
        }


        for (WayDescriptor n : sm.newWays) {
            if (josmMatch.match(n)) {
                md.addWay(n, "New way matches expression");
            }
        }
        for (WayDescriptor n : sm.deletedWays) {
            if (josmMatch.match(n)) {
                md.addWay(n, "Deleted way matches expression");
            }
        }
        for (WayDescriptor n : sm.waysWithChangedNodes) {
            if (josmMatch.match(n)) {
                md.addWay(n, "Way with changed nodes matches expression");
            }
        }
        for (WayChange n : sm.changedWays) {
            boolean after = josmMatch.match(n.after);
            boolean before = josmMatch.match(n.before);
            if (before && after) {
                md.addWay(n.after, "Changed way matches expression before and after change");
            } else if (before) {
                md.addWay(n.before, "Changed way matched expression before change");
            } else if (after) {
                md.addWay(n.after, "Changed way matches expression after change");
            }
        }
        return md;
    }
}
