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
import org.springframework.web.bind.annotation.RequestParam;

import fr.openstreetmap.watch.DatabaseManager;
import fr.openstreetmap.watch.Engine;
import fr.openstreetmap.watch.model.db.Alert;
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
    
    private LastAugmentedDownloader lad;
    @Autowired
    public void setLastAugmentedDownloader(LastAugmentedDownloader lad) {
    	this.lad = lad;
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