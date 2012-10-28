package fr.openstreetmap.watch.util;

import java.io.IOException;
import java.lang.reflect.Field;

import org.json.JSONException;
import org.json.JSONWriter;

public class JSONUtils {
	public static void writeObject(JSONWriter wr, Object o) throws IOException {
		try {
			for (Field f : o.getClass().getDeclaredFields()) {
				if (f.getType() == Long.TYPE) {
					wr.key(f.getName()).value(f.getLong(o));
				} else if (f.getType() == Integer.TYPE) {
					wr.key(f.getName()).value(f.getInt(o));
				}else if (f.getType() == Boolean.TYPE) {
					wr.key(f.getName()).value(f.getBoolean(o));
				} else if (f.getType() == String.class) {
					wr.key(f.getName()).value(f.get(o));
				}
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	public static void writeStaticObject(JSONWriter wr, Class<?> c, boolean createObject) throws IOException {
		try {
			if (createObject) wr.object();
			for (Field f : c.getDeclaredFields()) {
				if (f.getType() == Long.TYPE) {
					wr.key(f.getName()).value(f.getLong(null));
				} else if (f.getType() == Integer.TYPE) {
					wr.key(f.getName()).value(f.getInt(null));
				}else if (f.getType() == Boolean.TYPE) {
					wr.key(f.getName()).value(f.getBoolean(null));
				} else if (f.getType() == String.class) {
					wr.key(f.getName()).value(f.get(null));
				}
			}
			if (createObject) wr.endObject();

		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
