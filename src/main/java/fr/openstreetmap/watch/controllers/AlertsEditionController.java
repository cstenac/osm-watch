package fr.openstreetmap.watch.controllers;

import java.io.IOException;

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
    	 User ud = AuthenticationHandler.verityAuth(req, dbManager);
         if (ud == null) {
             resp.sendError(403, "Not authenticated");
             return;
         }
         resp.setContentType("application/json");
         JSONWriter wr = new JSONWriter(resp.getWriter());
         try {
			wr.object().key("alerts").array();
			for (Alert ad : ud.getAlerts()) {
				wr.object();
				if (ad.getPolygonWKT() != null) wr.key("polygon").value(ad.getPolygonWKT());
				if (ad.getWatchedTags() != null) wr.key("tags").value(ad.getWatchedTags());
				wr.key("key").value(ad.getUniqueKey());
				wr.endObject();
			}
			wr.endArray().endObject();
			
		} catch (JSONException e) {
			logger.error(e);
			throw new IOException(e);
		}
         
    }

    @RequestMapping(value="/api/new_alert")
    public void newAlert(@RequestParam("tags") String tags, 
    					@RequestParam("wkt") String wkt,
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