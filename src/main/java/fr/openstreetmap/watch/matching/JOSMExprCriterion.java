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
        System.out.println("COMPILED TO "  + josmMatch);
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
            if (josmMatch.match(n.after)) {
                md.addNode(n.after, "Changed node matches expression AFTER");
            }
            if (josmMatch.match(n.before)) {
                md.addNode(n.before, "Changed node matches expression BEFORE");
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
//            System.out.println("cnway " + n.id);
            if (josmMatch.match(n)) {
//                System.out.println("cnway MATCH");
                md.addWay(n, "Way with changed nodes matches expression");
            }
        }
        for (WayChange n : sm.matchingChangedWays) {
//            System.out.println("cway " + n.id);
            if (josmMatch.match(n.after)) {
//                System.out.println("cway MATCH");
                md.addWay(n.after, "Changed way matches expression AFTER");
            }
            if (josmMatch.match(n.before)) {
//                System.out.println("cway MATCH");
                md.addWay(n.before, "Changed way matches expression BEFORE");
            }
        }
        return md;
    }
}
