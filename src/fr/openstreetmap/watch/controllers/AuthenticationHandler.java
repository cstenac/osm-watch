package fr.openstreetmap.watch.controllers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sun.security.pkcs11.Secmod.DbMode;

import fr.openstreetmap.watch.DatabaseManager;
import fr.openstreetmap.watch.XMLUtils;
import fr.openstreetmap.watch.model.UserDesc;

public class AuthenticationHandler {
    static final String request_token_url = "http://www.openstreetmap.org/oauth/request_token"        ;                                                                                                                     
    static final String access_token_url = "http://www.openstreetmap.org/oauth/access_token"     ;                                                                                                                          
    static final String authorize_token_url = "http://www.openstreetmap.org/oauth/authorize";
    
    static final String callbackUrl = "http://localhost:8080/osm-watch/auth_callback";
    static final String afterLoginUrl = "http://localhost:8080/osm-watch/";
    
    static final String consumerKey = "A0LiKR5qp3I01aO6SWUbvo2xlAnBimQaaKwTL6V0";
    static final String consumerSecret = "mcPHW4JDYQgek4xLMiwyAfWKGC2a07azTbMTQIUu";

    public static Map<String, String> parseCookies(HttpServletRequest req) {
        Map<String, String> ret = new HashMap<String, String>();
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                ret.put(c.getName(), c.getValue());
            }
        }
        return ret;
    }
    
    public static UserDesc verityAuth(HttpServletRequest req, DatabaseManager dbManager) {
        Map<String, String> map = parseCookies(req);
        String cookieAT = map.get("access_token");
        if (cookieAT == null) {
            return null; // Not authenticated at all
        }
        
        Query q = dbManager.getEM().createQuery ("SELECT x FROM UserDesc x WHERE accessToken= ?1");
        q.setParameter(1, cookieAT);
        List<UserDesc> l = q.getResultList();
        if (l.size() == 0) {
            logger.warn("No valid user for this access token");
            return null;
        }
        return l.get(0);
    }
  
    public static void authenticate(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        OAuthConsumer consumer = new DefaultOAuthConsumer(consumerKey, consumerSecret);
        OAuthProvider provider = new DefaultOAuthProvider(request_token_url, access_token_url, authorize_token_url);

        String authUrl = provider.retrieveRequestToken(consumer, callbackUrl);

        resp.addCookie(new Cookie("request_token", consumer.getToken()));
        resp.addCookie(new Cookie("request_token_secret", consumer.getTokenSecret()));
        resp.setStatus(302);
        resp.setHeader("Location", authUrl);
        return;
    }

    public static UserDesc processAuthReturn(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        OAuthConsumer consumer = new DefaultOAuthConsumer(consumerKey, consumerSecret);
        OAuthProvider provider = new DefaultOAuthProvider(request_token_url, access_token_url, authorize_token_url);
        
        Map<String, String> map = parseCookies(req);
        if (!map.containsKey("request_token")) {
            throw new IOException("request_token not in session");
        }
        consumer.setTokenWithSecret(map.get("request_token"),  map.get("request_token_secret"));
        
        String verifier = req.getParameter("oauth_verifier");

        provider.setOAuth10a(true);
        provider.retrieveAccessToken(consumer, verifier);

        // TODO: remove cookie request_token and request_token_secret
        
        /* Fetch the uid and user name from OSM */
        URL url = new URL("http://api.openstreetmap.org/api/0.6/user/details");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();

        consumer.sign(request);
        request.connect();
        String osmResponse = IOUtils.toString(request.getInputStream());
        
        UserDesc ud = new UserDesc();
        
        Document doc = XMLUtils.parse(osmResponse);
        Element e = (Element)XMLUtils.xpath(doc, "/osm/user").iterator().next();
        ud.setAccessToken(consumer.getToken());
        ud.setAccessTokenSecret(consumer.getTokenSecret());
        ud.setScreenName(e.getAttribute("display_name"));
        ud.setOsmId(Long.parseLong(e.getAttribute("id")));
        return ud;
    }
    
    private static Logger logger = Logger.getLogger("osm.watch.auth");
}