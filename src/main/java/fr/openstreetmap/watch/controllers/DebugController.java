package fr.openstreetmap.watch.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.openstreetmap.watch.DatabaseManager;
import fr.openstreetmap.watch.Engine;
import fr.openstreetmap.watch.State;
import fr.openstreetmap.watch.model.db.Alert;
import fr.openstreetmap.watch.model.db.User;
import fr.openstreetmap.watch.parsers.LastAugmentedDownloader;
import fr.openstreetmap.watch.util.JSONUtils;

@Controller
public class DebugController {
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

	private LastAugmentedDownloader lad;
	@Autowired
	public void setLastAugmentedDownloader(LastAugmentedDownloader lad) {
		this.lad = lad;
	}

	@RequestMapping(value="/debug/get_state")
	public void getState(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		resp.setContentType("application/json");
		JSONWriter wr = new JSONWriter(resp.getWriter());
		JSONUtils.writeStaticObject(wr, State.class, true);
	}

	@RequestMapping(value="/debug/list_all_alerts")
	public void listAllAlerts(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		logger.info("Listing all alerts");
		dbManager.begin();
		try {
			List<Alert> la = dbManager.getEM().createQuery("SELECT x From Alert x").getResultList();

			Collections.sort(la, new Comparator<Alert>() {
				@Override
				public int compare(Alert arg0, Alert arg1) {
					return (int)(arg0.getCreationTimestamp() - arg1.getCreationTimestamp());
				}
			});

			resp.setContentType("application/json");
			JSONWriter wr = new JSONWriter(resp.getWriter());
			wr.object().key("alerts").array();

			for (Alert ad : la) {
				wr.object();
//				if (ad.getPolygonWKT() != null) wr.key("polygon").value(ad.getPolygonWKT());
				if (ad.getFilterClass() != null) wr.key("filterClass").value(ad.getFilterClass());
				if (ad.getFilterParams() != null) wr.key("filterParams").value(ad.getFilterParams());
				wr.key("creation_timestamp").value(ad.getCreationTimestamp());
				wr.key("name").value(ad.getName());
				wr.key("id").value(ad.getId());
				wr.key("key").value(ad.getUniqueKey());
				wr.key("publicAlert").value(ad.isPublicAlert());
				wr.key("emailEnabled").value(ad.isEmailEnabled());
				if (ad.getAlertMatches() != null) {
					wr.key("nb_matches").value(ad.getAlertMatches().size());
				} else {
					wr.key("nb_matches").value(0);
				}
				wr.endObject();
			}
			wr.endArray().endObject();

		} catch (Exception e) {
			logger.error(e);
			throw new IOException(e);
		} finally {
			dbManager.rollback();
		}
	}

	@RequestMapping(value="/debug/send_augmented_diff")
	public void newAlert(@RequestParam("file") String file, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// Non transactional

		String content = FileUtils.readFileToString(new File(file));
		try {
			engine.handleAugmentedDiff(content, 2);
		} catch (Exception e) {
			logger.error("Failed", e);
			resp.sendError(500, "Failed: " + e.getMessage());
		}
	}

	@RequestMapping(value="/debug/next_augmented_diff")
	public void nextAugmentedDiff(HttpServletResponse resp) throws IOException {
		// Non transactional
		try {
			lad.run();
			logger.info("Augmented diff handling done");
		} catch (Exception e) {
			logger.error("Failed", e);
			resp.sendError(500, "Failed: " + e.getMessage());
		}
	}

	@RequestMapping(value="/debug/add_alert_to_filter")
	public void newAlert(String uid, String tags, String polygons, HttpServletResponse resp) throws IOException {
		// Non transactional
		Alert ad = new Alert();
		ad.setPolygonWKT(polygons);
		ad.setFilterParams(tags);
		try {
			engine.addAlertToSpatialFilter(ad);
		} catch (Exception e) {
			logger.error("Failed", e);
			resp.sendError(500, "Failed: " + e.getMessage());
		}
	}

	private static Logger logger = Logger.getLogger("osm.watch.controller");
}