package fr.openstreetmap.watch.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.management.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.openstreetmap.watch.DatabaseManager;
import fr.openstreetmap.watch.model.db.Alert;
import fr.openstreetmap.watch.model.db.AlertMatch;
import fr.openstreetmap.watch.model.db.User;
import fr.openstreetmap.watch.util.SimpleXMLWriter;

/**
 * Gets matches data for an alert
 */
@Controller
public class AlertDisplayController {
	private DatabaseManager dbManager;
	@Autowired
	public void setDatabaseManager(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	@RequestMapping(value="/api/alert_data")
	public void newAlert(@RequestParam("key") String key, 
			HttpServletRequest req, HttpServletResponse resp) throws IOException {

		dbManager.begin();
		try {
			javax.persistence.Query q = dbManager.getEM().createQuery("SELECT x FROM Alert x where uniqueKey = ?1");
			q.setParameter(1, key);
			List<Alert> aa = q.getResultList();
			if (aa.size() == 0) {
				resp.sendError(500, "Alert does not exist");
				return;
			}
			Alert a = aa.get(0);

			
			List<AlertMatch> amList = new ArrayList<AlertMatch>();
			amList.addAll(a.getAlertMatches());
			
			Collections.sort(amList, new Comparator<AlertMatch>() {
                @Override
                public int compare(AlertMatch o1, AlertMatch o2) {
                    if (o1.getMatchTimestamp() > o2.getMatchTimestamp()) {
                        return -1;
                    } else if (o1.getMatchTimestamp() < o2.getMatchTimestamp()) {
                        return 1;
                    } else if (o1.getId() < o2.getId()) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
			});
		
			resp.setContentType("application/json");
			JSONWriter wr = new JSONWriter(resp.getWriter());
		
			wr.object();
			
			wr.key("matches").array();
			int matches = 0;
			for (AlertMatch am : amList) {
				if (matches++ == 100) break;
				
				wr.object();
				
				wr.key("changeset").value(am.getChangesetId());
				wr.key("matchDate").value(am.getMatchTimestamp());
				wr.key("reason").value(am.getReason());
				// TODO: bbox
				
				wr.endObject();
			}
			wr.endArray();
			
			wr.endObject();
		} catch (Exception e) {
			logger.error("Failed to write Alert data", e);
			resp.setStatus(500);
			resp.setContentType("application/json");
			resp.getWriter().write("{\"ok\": \"0\"}");
		} finally {
			dbManager.rollback();
		}
	}

	private static Logger logger = Logger.getLogger("osm.watch.controller");
}