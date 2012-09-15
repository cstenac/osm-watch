package fr.openstreetmap.watch;

import java.util.HashSet;
import java.util.Set;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import fr.openstreetmap.watch.criteria.TagsCriterion;
import fr.openstreetmap.watch.model.db.AlertDesc;

/**
 * Runtime representation of an alert
 */
public class Alert {
    public Alert() {}

    public Alert(AlertDesc desc) throws ParseException {
        this.desc = desc;

        if (desc.getWatchedTags() != null) {
            String[] tags = desc.getWatchedTags().split(",");
            Set<String> s = new HashSet<String>();
            for (String tag : tags) s.add(tag);
            tagsFilter = new TagsCriterion(s);
        }
        
        if (desc.getPolygonWKT() != null) {
            WKTReader reader = new WKTReader();
            polygonFilter = (Polygon)reader.read(desc.getPolygonWKT());
        }
    }
    
    public AlertDesc desc;
    
    public long id;
	public String user;
	public String uniqueKey;
	
	TagsCriterion tagsFilter;
	public Polygon polygonFilter;
}
