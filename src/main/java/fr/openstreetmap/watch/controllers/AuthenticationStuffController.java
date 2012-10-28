package fr.openstreetmap.watch.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.openstreetmap.watch.ApplicationConfigurator;
import fr.openstreetmap.watch.DatabaseManager;
import fr.openstreetmap.watch.model.db.Alert;
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
        dbManager.begin();
        if (AuthenticationHandler.verityAuth(req, dbManager) != null) {
            resp.sendError(400, "Already authenticated");
            dbManager.rollback();
            return;
        }
        try {
            AuthenticationHandler.authenticate(req, resp);
        } catch (Exception e) {
            logger.error("Failed to authenticate", e); 
            resp.sendError(401, "Failed to authenticate");
        } finally {
            dbManager.rollback();
        }
    }

    @RequestMapping(value="/auth_callback")
    public void authCallback(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        dbManager.begin();

        System.out.println("oauth callback");
        if (AuthenticationHandler.verityAuth(req, dbManager) != null) {
            resp.sendError(400, "Already authenticated");
            dbManager.rollback();
            return;
        }
        try {
            System.out.println("Processing OAuth callback");
            User ud = AuthenticationHandler.processAuthReturn(dbManager, req, resp);
            System.out.println("NOW AUTHENTICATED !");
            resp.setStatus(200);
            //            resp.addHeader("Location", ApplicationConfigurator.getBaseURL());
            resp.setContentType("text/html");
            resp.getWriter().write("<html><head><meta http-equiv=\"refresh\" content=\"0; URL=" +  ApplicationConfigurator.getBaseURL() + "/\" /></head></html>");
            dbManager.commit();
        } catch (Throwable e) {
            logger.error("Failed to authenticate", e); 
            resp.sendError(401, "Failed to authenticate");
            dbManager.rollback();
        }
    }
    
    @RequestMapping(value="/api/set_email_address")
    public void setEmailAddress(String email, HttpServletRequest req, HttpServletResponse resp) throws IOException {
          dbManager.begin();
          try {
              User ud = AuthenticationHandler.verityAuth(req, dbManager);
              if (ud == null) {
                  resp.sendError(403, "Not authenticated");
                  return;
              }
              ud.setEmailAddress(email);
              dbManager.getEM().persist(ud);
              dbManager.commit();
          } catch (Exception e) {
        	  dbManager.rollback();
          }
    }

    private static Logger logger = Logger.getLogger("osm.watch.controller");
}
