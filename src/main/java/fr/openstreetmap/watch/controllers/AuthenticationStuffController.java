package fr.openstreetmap.watch.controllers;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.openstreetmap.watch.DatabaseManager;
import fr.openstreetmap.watch.model.db.User;

@Controller
public class AuthenticationStuffController {
    private DatabaseManager dbManager;
    @Autowired
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @RequestMapping(value="/authenticate")
    public void authenticate(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        BasicConfigurator.configure();
        if (AuthenticationHandler.verityAuth(req, dbManager) != null) {
            resp.sendError(400, "Already authenticated");
            return;
        }
        try {
            AuthenticationHandler.authenticate(req, resp);
        } catch (Exception e) {
            logger.error("Failed to authenticate", e); 
            resp.sendError(401, "Failed to authenticate");
        }
    }

    @RequestMapping(value="/auth_callback")
    public String authCallback(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        System.out.println("oauth callback");
        if (AuthenticationHandler.verityAuth(req, dbManager) != null) {
            resp.sendError(400, "Already authenticated");
            return "home";
        }
        try {
            System.out.println("Processing OAuth callback");
            User ud = AuthenticationHandler.processAuthReturn(dbManager, req, resp);
            return "home";
            //resp.addHeader("Location", AuthenticationHandler.afterLoginUrl);
        } catch (Throwable e) {
            logger.error("Failed to authenticate", e); 
            resp.sendError(401, "Failed to authenticate");
            return "home";
        }
    }

    private static Logger logger = Logger.getLogger("osm.watch.controller");
}
