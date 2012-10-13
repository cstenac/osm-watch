package fr.openstreetmap.watch.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.openstreetmap.watch.DatabaseManager;
import fr.openstreetmap.watch.Engine;
import fr.openstreetmap.watch.matching.MatchableAlert;
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

    @SuppressWarnings("unchecked")
    @RequestMapping(value="/api/list_alerts")
    public synchronized void listAlerts(HttpServletRequest req, HttpServletResponse resp,
            @RequestParam(value="public", defaultValue="false")boolean publicAlerts) throws IOException {
        
        logger.info("Listing alerts");
        dbManager.begin();
        try {
            List<Alert> la = new ArrayList<Alert>();
            if (publicAlerts) {
                Query q = dbManager.getEM().createQuery("SELECT x FROM Alert x where publicAlert=true");
                la.addAll(q.getResultList());
            } else {
                User ud = AuthenticationHandler.verityAuth(req, dbManager);
                if (ud == null) {
                    resp.sendError(403, "Not authenticated");
                    return;
                }
                if (ud.getAlerts() != null) {
                	la.addAll(ud.getAlerts());
                }
                logger.info("Listing private alerts, have " + la.size());
            }

            Collections.sort(la, new Comparator<Alert>() {
                @Override
                public int compare(Alert arg0, Alert arg1) {
                    return (int)(arg0.getCreationTimestamp() - arg1.getCreationTimestamp());
                }
            });

            resp.setContentType("application/json");
            JSONWriter wr = new JSONWriter(resp.getWriter());
            wr.object().key("alerts").array();

            logger.info("List" + dbManager.getEM());

            for (Alert ad : la) {
                wr.object();
                if (ad.getPolygonWKT() != null) wr.key("polygon").value(ad.getPolygonWKT());
                if (ad.getFilterClass() != null) wr.key("filterClass").value(ad.getFilterClass());
                if (ad.getFilterParams() != null) wr.key("filterParams").value(ad.getFilterParams());
                wr.key("creation_timestamp").value(ad.getCreationTimestamp());
                wr.key("name").value(ad.getName());
                wr.key("id").value(ad.getId());
                wr.key("key").value(ad.getUniqueKey());
                wr.key("publicAlert").value(ad.isPublicAlert());
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

    @RequestMapping(value="/api/delete_alert")
    public synchronized void deleteAlert(@RequestParam("key") String key, 
            HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("Removing alert " + key);

        dbManager.begin();
        try {
            User ud = AuthenticationHandler.verityAuth(req, dbManager);
            if (ud == null) {
                resp.sendError(403, "Not authenticated");
                return;
            }
            dbManager.deleteAlert(key);
            dbManager.commit();
            logger.info("Alert deletion successful");
            writeJSONOK(resp);
        } catch (IOException e) {
            dbManager.rollback();
            throw e;
        }
    }

    
    @RequestMapping(value="/api/set_alert_public")
    public synchronized void setAlertPublic(@RequestParam("key") String key, 
            HttpServletRequest req, HttpServletResponse resp, @RequestParam(value="public")boolean publicAlert) throws IOException {
        logger.info("Editing alert " + key);

        dbManager.begin();
        try {
            User ud = AuthenticationHandler.verityAuth(req, dbManager);
            if (ud == null) {
                resp.sendError(403, "Not authenticated");
                return;
            }
            Alert a = dbManager.getAlertByKey(key);
            if (a != null) {
                if (a.getUser().getOsmId() != ud.getOsmId()) {
                    throw new IOException("Wrong user, you can't edit this alert");
                }
                a.setPublicAlert(publicAlert);
                dbManager.getEM().persist(a);
            }
            writeJSONOK(resp);
            dbManager.commit();
        } catch (IOException e) {
            dbManager.rollback();
            throw e;
        }
    }

    @RequestMapping(value="/api/new_alert")
    public synchronized void newAlert(
    		@RequestParam(value="filterClass", required=false) String filterClass,
    		@RequestParam(value="filterParams", required=false) String filterParams, 
            @RequestParam(value="wkt", required=false) String wkt,
            @RequestParam("name") String name,
            HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("Creating alert");

        dbManager.begin();
        try {
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
            ad.setFilterParams(filterParams);
            ad.setFilterClass(filterClass);
            ad.setName(name);
            ad.setUniqueKey(SecretKeyGenerator.generate());

            /* Check syntax */
            try {
                new MatchableAlert(ad);
            } catch (Exception e) {
                logger.error("Failed to create alert, can't instantiate it", e);
                writeJSONNOK(resp, "Failed to instantiate alert: " + e.getMessage());
                dbManager.rollback();
                return;
            }

            dbManager.getEM().persist(ad);
            dbManager.getEM().flush();
            dbManager.commit();

            engine.addAlertToSpatialFilter(ad);
            logger.info("Alert created successfully");
            writeJSONOK(resp);
        } catch (Exception e) {
            logger.error("Failed to create alert", e);
            dbManager.rollback();
            writeJSONNOK(resp, e.getMessage());
        }
    }
    
    
    private void writeJSONOK(HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.getWriter().write("{\"ok\": \"true\"}");
    }
    private void writeJSONNOK(HttpServletResponse resp, String error) throws IOException {
        resp.setContentType("application/json");
        resp.setStatus(500);
        resp.getWriter().write("{\"ok\": \"false\", \"error\" + " + error.replace("\"", "\\\"") + "}");
    }

    private static Logger logger = Logger.getLogger("osm.watch.controller");
}