package fr.openstreetmap.watch.matching;

import java.lang.reflect.Constructor;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import fr.openstreetmap.watch.matching.josmexpr.JOSMExprFilter;
import fr.openstreetmap.watch.matching.josmexpr.SearchCompiler.ParseError;
import fr.openstreetmap.watch.matching.misc.LegacyTagKeysFilter;
import fr.openstreetmap.watch.model.db.Alert;

/**
 * Runtime representation of an alert.
 * This is instantiated from the description of the alert in the database.
 * 
 * It contains the runtime filter class and the match polygon for the spatial filter.
 * It acts as the filter factory.
 */
public class MatchableAlert {
	public MatchableAlert() {}

	public MatchableAlert(Alert desc) throws Exception {
		this.desc = desc;

		String clazz = desc.getFilterClass();
		if (clazz != null) {
			/* Filters must have a constructor that takes the parameters string */
			@SuppressWarnings("unchecked")
			Constructor<Filter> ctor = (Constructor<Filter>) Class.forName(clazz).getConstructor(String.class);
			filter = ctor.newInstance(desc.getFilterParams());
		}

		if (desc.getPolygonWKT() != null) {
			WKTReader reader = new WKTReader();
			polygon = (Polygon)reader.read(desc.getPolygonWKT());
		}
	}

	public Filter getFilter() {
		return filter;
	}
	
	public Polygon getPolygon() {
		return polygon;
	}

	public Alert desc;

	private Filter filter;
	Polygon polygon; // package protected for ut
}
