package fr.openstreetmap.watch.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.openstreetmap.watch.DatabaseManager;
import fr.openstreetmap.watch.model.AlertDesc;
import fr.openstreetmap.watch.model.UserDesc;

@Controller
public class AlertsEditionController {
    private DatabaseManager dbManager;
    @Autowired
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @RequestMapping(value="/api/new_alert")
    public void newAlert(String tags, String wkt, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDesc ud = AuthenticationHandler.verityAuth(req, dbManager);
        if (ud == null) {
            resp.sendError(403, "Not authenticated");
            return;
        }
        AlertDesc ad = new AlertDesc();
        ad.setUser(ud);
        if (wkt != null && wkt.length() > 0 ) {
            ad.setPolygonWKT(wkt);
        }
        if (tags != null && tags.length() > 0) {
            ad.setWatchedTags(tags);
        }
        resp.setContentType("application/json");
        resp.getWriter().write("{\"ok\": \"1\"}");
    }
    
    private static Logger logger = Logger.getLogger("osm.watch.controller");
}