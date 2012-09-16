package fr.openstreetmap.watch.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.openstreetmap.watch.DatabaseManager;
import fr.openstreetmap.watch.Engine;
import fr.openstreetmap.watch.model.db.Alert;
import fr.openstreetmap.watch.model.db.User;
import fr.openstreetmap.watch.util.SecretKeyGenerator;

@Controller
public class AlertsEditionController {
	private DatabaseManager dbManager;
	@Autowired
	public void setDatabaseManager(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	private Engine engine;
	@Autowired
	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	@RequestMapping(value="/api/list_alerts")
	public void listAlerts(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		dbManager.begin();
		try {
			User ud = AuthenticationHandler.verityAuth(req, dbManager, false);
			if (ud == null) {
				resp.sendError(403, "Not authenticated");
				return;
			}
			resp.setContentType("application/json");
			JSONWriter wr = new JSONWriter(resp.getWriter());

			wr.object().key("alerts").array();
			List<Alert> la = new ArrayList<Alert>();
			la.addAll(ud.getAlerts());
			Collections.sort(la, new Comparator<Alert>() {
				@Override
				public int compare(Alert arg0, Alert arg1) {
					return (int)(arg0.getCreationTimestamp() - arg1.getCreationTimestamp());
				}
			});
			for (Alert ad : la) {
				wr.object();
				if (ad.getPolygonWKT() != null) wr.key("polygon").value(ad.getPolygonWKT());
				if (ad.getWatchedTags() != null) wr.key("tags").value(ad.getWatchedTags());
				wr.key("creation_timestamp").value(ad.getCreationTimestamp());
				wr.key("name").value(ad.getName());
				wr.key("id").value(ad.getId());
				wr.key("key").value(ad.getUniqueKey());
				if (ad.getAlertMatches() != null) {
					wr.key("nb_matches").value(ad.getAlertMatches().size());
				} else {
					wr.key("nb_matches").value(0);
				}
				wr.endObject();
			}
			wr.endArray().endObject();

		} catch (JSONException e) {
			logger.error(e);
			throw new IOException(e);
		} finally {
			dbManager.rollback();
		}

	}

	@RequestMapping(value="/api/delete_alert")
	public void newAlert(@RequestParam("key") String key, 
			HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User ud = AuthenticationHandler.verityAuth(req, dbManager);
		if (ud == null) {
			resp.sendError(403, "Not authenticated");
			return;
		}
		dbManager.deleteAlert(key);
		resp.setStatus(200);
		resp.setContentType("application/json");
		resp.getWriter().write("{\"ok\": \"0\"}");
	}

	@RequestMapping(value="/api/new_alert")
	public void newAlert(@RequestParam("tags") String tags, 
			@RequestParam("wkt") String wkt,
			@RequestParam("name") String name,
			HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User ud = AuthenticationHandler.verityAuth(req, dbManager);
		if (ud == null) {
			resp.sendError(403, "Not authenticated");
			return;
		}
		Alert ad = new Alert();
		ad.setUser(ud);
		if (wkt != null && wkt.length() > 0 ) {
			ad.setPolygonWKT(wkt);
		}
		if (tags != null && tags.length() > 0) {
			ad.setWatchedTags(tags);
		}
		ad.setName(name);
		ad.setUniqueKey(SecretKeyGenerator.generate());

		dbManager.getEM().getTransaction().begin();
		try {
			dbManager.getEM().persist(ad);
			dbManager.getEM().flush();
			dbManager.getEM().getTransaction().commit();

			resp.setContentType("application/json");
			resp.getWriter().write("{\"ok\": \"1\"}");


		} catch (Exception e) {
			logger.error("Failed to create alert", e);
			dbManager.getEM().getTransaction().rollback();
			resp.setStatus(500);
			resp.setContentType("application/json");
			resp.getWriter().write("{\"ok\": \"0\"}");
		}
		try {
			engine.addAlertToSpatialFilter(ad);
		} catch (Exception e) {
			logger.error("Failed to add alert to engine", e);
			resp.setStatus(500);
			resp.setContentType("application/json");
			resp.getWriter().write("{\"ok\": \"0\"}");
		}

	}

	private static Logger logger = Logger.getLogger("osm.watch.controller");
}