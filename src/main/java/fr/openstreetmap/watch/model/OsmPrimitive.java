package fr.openstreetmap.watch.model;
// License: GPL. Copyright 2007 by Immanuel Scholz and others

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An OSM primitive can be associated with a key/value pair. It can be created, deleted
 * and updated within the OSM-Server.
 *
 * Although OsmPrimitive is designed as a base class, it is not to be meant to subclass
 * it by any other than from the package {@link org.openstreetmap.josm.data.osm}. The available primitives are a fixed set that are given
 * by the server environment and not an extendible data stuff.
 *
 * @author imi
 */
public abstract class OsmPrimitive {
    
    public long getId() {
        return id;
    }
    
    public long getChangesetId() {
        return changeset;
    }

    public long getVersion()  {
        return version;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getUser() {
        return user;
    }
    
    public boolean hasKeys() {
        return tags != null &&  tags.size() > 0;
    }
    
    public Map<String, String> getKeys() {
        if (tags == null) tags = new HashMap<String, String>();
        return tags;
    }
    
    public Set<String> keySet() {
        if (tags == null) tags = new HashMap<String, String>();
        return tags.keySet();
    }
    
    public String get(String key) {
        return tags == null ? null : tags.get(key);
    }
    
    
    public boolean hasTag(String tag) {
    	return get(tag) != null;
    }
    
    public String getSourceTag() {
    	return get("source");
    }
    
    
    public Map<String, String> tags;
    public long timestamp;
    public long version;
    public long id;
    public long changeset;

    public String user;
    public long uid;
}