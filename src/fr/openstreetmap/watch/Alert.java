package fr.openstreetmap.watch;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

import fr.openstreetmap.watch.criteria.TagsCriterion;

public class Alert {
    public long id;
	public String user;
	public String uniqueKey;
	
	public List<TagsCriterion> tagFilters = new ArrayList<TagsCriterion>();
	
	public Polygon polygonFilter;
}
