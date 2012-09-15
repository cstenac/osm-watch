package fr.openstreetmap.watch.matching;

import java.util.HashSet;
import java.util.Set;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import fr.openstreetmap.watch.model.db.Alert;

/**
 * Runtime representation of an alert
 */
public class RuntimeAlert {
    public RuntimeAlert() {}

    public RuntimeAlert(Alert desc) throws ParseException {
        this.desc = desc;

        if (desc.getWatchedTags() != null) {
            String[] tags = desc.getWatchedTags().split(",");
            Set<String> s = new HashSet<String>();
            for (String tag : tags) s.add(tag);
            tagsFilter = new TagKeysCriterion(s);
        }
        
        if (desc.getPolygonWKT() != null) {
            WKTReader reader = new WKTReader();
            polygonFilter = (Polygon)reader.read(desc.getPolygonWKT());
        }
    }
    
    public Alert desc;
    
    public long id;
	public String user;
	public String uniqueKey;
	
	public TagKeysCriterion tagsFilter;
	public Polygon polygonFilter;
}
