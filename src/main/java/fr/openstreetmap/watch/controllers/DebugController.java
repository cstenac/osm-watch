package fr.openstreetmap.watch.controllers;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.openstreetmap.watch.DatabaseManager;
import fr.openstreetmap.watch.Engine;
import fr.openstreetmap.watch.model.db.Alert;
import fr.openstreetmap.watch.model.db.User;
import fr.openstreetmap.watch.parsers.LastAugmentedDownloader;

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

    @RequestMapping(value="/debug/send_augmented_diff")
    public void newAlert(String file, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String content = FileUtils.readFileToString(new File(file));
        try {
            engine.handleAugmentedDiff(content);
        } catch (Exception e) {
            logger.error("Failed", e);
            resp.sendError(500, "Failed: " + e.getMessage());
        }
    }
    
    @RequestMapping(value="/debug/next_augmented_diff")
    public void nextAugmentedDiff(HttpServletResponse resp) throws IOException {
        try {
        	LastAugmentedDownloader lad = new LastAugmentedDownloader();
        	lad.setEngine(engine);
        	lad.run();
        } catch (Exception e) {
            logger.error("Failed", e);
            resp.sendError(500, "Failed: " + e.getMessage());
        }
    }
    
    @RequestMapping(value="/debug/add_alert_to_filter")
    public void newAlert(String uid, String tags, String polygons, HttpServletResponse resp) throws IOException {
        Alert ad = new Alert();
        ad.setPolygonWKT(polygons);
        ad.setWatchedTags(tags);
        try {
            engine.addAlertToSpatialFilter(ad);
        } catch (Exception e) {
            logger.error("Failed", e);
            resp.sendError(500, "Failed: " + e.getMessage());
        }
    }
    
    private static Logger logger = Logger.getLogger("osm.watch.controller");
}