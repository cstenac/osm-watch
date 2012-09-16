package fr.openstreetmap.watch.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.openstreetmap.watch.DatabaseManager;
import fr.openstreetmap.watch.model.db.User;

@Controller
public class HomePageController {
    private DatabaseManager dbManager;
    @Autowired
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @RequestMapping(value="/")
    public String home(HttpServletRequest req, ModelMap map) throws IOException {
        User ud = AuthenticationHandler.verityAuth(req, dbManager);
        map.addAttribute("ud", ud);
        return "home";
    }
    @RequestMapping(value="/home")
    public String home2(HttpServletRequest req, ModelMap map) throws IOException {
        User ud = AuthenticationHandler.verityAuth(req, dbManager);
        map.addAttribute("ud", ud);
        return "home";
    }
    
    private static Logger logger = Logger.getLogger("osm.watch.controller");
}