package fr.openstreetmap.watch.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.openstreetmap.watch.DatabaseManager;
import fr.openstreetmap.watch.model.UserDesc;

@Controller
public class AlertsEditionController {
    private DatabaseManager dbManager;
    @Autowired
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @RequestMapping(value="/api/new_alert")
    public void newAlert(String watchedTags, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDesc ud = AuthenticationHandler.verityAuth(req, dbManager);
        if (ud == null) {
            resp.sendError(403, "Not authenticated");
            return;
        }
        resp.getWriter().write("Add alert for " + ud.getScreenName());
    }
    
    private static Logger logger = Logger.getLogger("osm.watch.controller");
}