package fr.openstreetmap.watch.criteria.josmexpr;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

import fr.openstreetmap.watch.matching.josmexpr.SearchCompiler;
import fr.openstreetmap.watch.matching.josmexpr.SearchCompiler.Match;
import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.WayDescriptor;

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
    public void values() throws Exception {
        String expr = "Street";
        Match m = SearchCompiler.compile(expr, true, true);
        System.out.println(m.toString());
        
        NodeDescriptor nd = new NodeDescriptor();
        nd.tags = new HashMap<String, String>();
        
        nd.tags.put("name", "Baker Street");
        assertTrue(m.match(nd));
        
        nd.tags.clear();
        nd.tags.put("Street", "name");
        assertTrue(m.match(nd));

        nd.tags.clear();
    }
    
    @Test
    public void metas() throws Exception {
        String expr = "version:3";
        Match m = SearchCompiler.compile(expr, true, true);
        System.out.println(m.toString());
        
        NodeDescriptor nd = new NodeDescriptor();
        assertFalse(m.match(nd));
        
        nd.version = 3;
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
    @Test
    public void keysOnly() throws Exception {
        String expr = "highway=*";
        Match m = SearchCompiler.compile(expr, true, true);
        System.out.println(m.toString());
        
        NodeDescriptor nd = new NodeDescriptor();
        nd.tags = new HashMap<String, String>();
        assertFalse(m.match(nd));
        
        nd.tags.put("stuff", "highway");
        assertFalse(m.match(nd));
        
        nd.tags.put("highway", "tertiary");
        assertTrue(m.match(nd));
    }
    @Test
    public void valsOnly() throws Exception {
        String expr = "*=primary";
        Match m = SearchCompiler.compile(expr, true, true);
        System.out.println(m.toString());
        
        NodeDescriptor nd = new NodeDescriptor();
        nd.tags = new HashMap<String, String>();
        assertFalse(m.match(nd));
        
        nd.tags.put("primary", "highway");
        assertFalse(m.match(nd));
        
        nd.tags.put("highway", "primary");
        assertTrue(m.match(nd));
    }
    @Test
    public void regex() throws Exception {
        String expr = "highway=.*ary$";
        Match m = SearchCompiler.compile(expr, true, true);
        System.out.println(m.toString());
        
        NodeDescriptor nd = new NodeDescriptor();
        nd.tags = new HashMap<String, String>();
        assertFalse(m.match(nd));
        
        nd.tags.put("primary", "highway");
        assertFalse(m.match(nd));
        
        nd.tags.put("highway", "glarry");
        assertFalse(m.match(nd));
        nd.tags.put("highway", "secondary");
        assertTrue(m.match(nd));

    }
    @Test
    public void nbTags() throws Exception {
        String expr = "tags:1-3";
        Match m = SearchCompiler.compile(expr, true, true);
        System.out.println(m.toString());
        
        NodeDescriptor nd = new NodeDescriptor();
        nd.tags = new HashMap<String, String>();
        assertFalse(m.match(nd));
        
        nd.tags.put("tag1", "v1");
        assertTrue(m.match(nd));
        nd.tags.put("tag2", "v1");
        assertTrue(m.match(nd));
        nd.tags.put("tag3", "v1");
        assertTrue(m.match(nd));
        nd.tags.put("tag4", "v1");
        assertFalse(m.match(nd));
    }
    
    @Test
    public void type() throws Exception {
        String expr = "type:node && tags:1-3";
        Match m = SearchCompiler.compile(expr, true, true);
        System.out.println(m.toString());
        
        NodeDescriptor nd = new NodeDescriptor();
        nd.tags = new HashMap<String, String>();
        assertFalse(m.match(nd));
        
        nd.tags.put("tag1", "v1");
        assertTrue(m.match(nd));
        nd.tags.put("tag2", "v1");
        assertTrue(m.match(nd));
        nd.tags.put("tag3", "v1");
        assertTrue(m.match(nd));
        nd.tags.put("tag4", "v1");
        assertFalse(m.match(nd));
    }
    
    @Test
    public void nodes() throws Exception {
        String expr = "type:way && nodes:1-3";
        Match m = SearchCompiler.compile(expr, true, true);
        System.out.println(m.toString());
        
        WayDescriptor wd = new WayDescriptor();
        assertFalse(m.match(wd));

        wd.nodes.add(1l);
        assertTrue(m.match(wd));
        wd.nodes.add(1l);
        assertTrue(m.match(wd));
        wd.nodes.add(1l);
        assertTrue(m.match(wd));
        wd.nodes.add(1l);
        assertFalse(m.match(wd));
    }


}
