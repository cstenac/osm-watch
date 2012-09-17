package fr.openstreetmap.watch.criteria.josmexpr;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

import fr.openstreetmap.watch.matching.josmexpr.SearchCompiler;
import fr.openstreetmap.watch.matching.josmexpr.SearchCompiler.Match;
import fr.openstreetmap.watch.model.NodeDescriptor;

public class JOSMExprTest {
    @Test
    public void multitags() throws Exception {
        String expr = "highway:primary | highway:secondary | id:42";
        Match m = SearchCompiler.compile(expr, true, true);
        System.out.println(m.toString());
        
        NodeDescriptor nd = new NodeDescriptor();
        nd.id = 41;
        nd.tags = new HashMap<String, String>();
        
        assertFalse(m.match(nd));
        
        nd.tags.put("highway", "primary");
        assertTrue(m.match(nd));
        
        nd.tags.put("highway", "tertiary");
        assertFalse(m.match(nd));
        
        nd.tags.clear();
        assertFalse(m.match(nd));
        
        nd.id = 42;
        assertTrue(m.match(nd));
    }
    
    @Test
    public void keys() throws Exception {
        String expr = "highway | waterway";
        Match m = SearchCompiler.compile(expr, true, true);
        System.out.println(m.toString());
        
        NodeDescriptor nd = new NodeDescriptor();
        nd.tags = new HashMap<String, String>();
        
        assertFalse(m.match(nd));
        
        nd.tags.put("highway", "primary");
        assertTrue(m.match(nd));
        
        nd.tags.put("highway", "tertiary");
        assertTrue(m.match(nd));
        
        nd.tags.clear();
        assertFalse(m.match(nd));
        
        nd.tags.put("waterway", "tertiary");
        assertTrue(m.match(nd));
    }
}
