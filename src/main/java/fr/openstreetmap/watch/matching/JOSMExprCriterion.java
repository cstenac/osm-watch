package fr.openstreetmap.watch.matching;

import fr.openstreetmap.watch.matching.josmexpr.SearchCompiler;
import fr.openstreetmap.watch.matching.josmexpr.SearchCompiler.Match;
import fr.openstreetmap.watch.matching.josmexpr.SearchCompiler.ParseError;
import fr.openstreetmap.watch.model.NodeChange;
import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.WayChange;
import fr.openstreetmap.watch.model.WayDescriptor;

public class JOSMExprCriterion extends Criterion {
    Match josmMatch;

    public JOSMExprCriterion(String expr) throws ParseError {
        josmMatch = SearchCompiler.compile(expr, false, true);
    }

    @Override
    public MatchDescriptor matches(SpatialMatch sm) {
        MatchDescriptor md = new MatchDescriptor(sm);
        for (NodeDescriptor n : sm.matchingNewNodes) {
            if (josmMatch.match(n)) {
                md.addNode(n, "New node matches expression");
            }
        }
        for (NodeDescriptor n : sm.matchingDeletedNodes) {
            if (josmMatch.match(n)) {
                md.addNode(n, "Deleted node matches expression");
            }
        }
        for (NodeChange n : sm.matchingChangedNodes) {
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


        for (WayDescriptor n : sm.matchingNewWays) {
            if (josmMatch.match(n)) {
                md.addWay(n, "New way matches expression");
            }
        }
        for (WayDescriptor n : sm.matchingDeletedWays) {
            if (josmMatch.match(n)) {
                md.addWay(n, "Deleted way matches expression");
            }
        }
        for (WayDescriptor n : sm.matchingWaysWithChangedNodes) {
            if (josmMatch.match(n)) {
                md.addWay(n, "Way with changed nodes matches expression");
            }
        }
        for (WayChange n : sm.matchingChangedWays) {
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
