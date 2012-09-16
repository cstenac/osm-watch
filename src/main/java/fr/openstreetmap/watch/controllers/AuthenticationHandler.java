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

import fr.openstreetmap.watch.DatabaseManager;
import fr.openstreetmap.watch.model.db.User;
import fr.openstreetmap.watch.model.db.UserSession;
import fr.openstreetmap.watch.util.XMLUtils;

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

	public static User verityAuth(HttpServletRequest req, DatabaseManager dbManager) {
		return AuthenticationHandler.verityAuth(req, dbManager, true);
	}

	public static User verityAuth(HttpServletRequest req, DatabaseManager dbManager, boolean doTransaction) {
		Map<String, String> map = parseCookies(req);
		String cookieAT = map.get("access_token");
		if (cookieAT == null) {
			return null; // Not authenticated at all
		}
		if (doTransaction) dbManager.begin();
		try {
			Query q = dbManager.getEM().createQuery ("SELECT x FROM User x INNER JOIN x.sessions sess WHERE sess.accessToken= ?1");
			q.setParameter(1, cookieAT);
			List<User> l = q.getResultList();
			if (l.size() == 0) {
				logger.warn("No valid user for this access token");
				return null;
			}
			User u = l.get(0);
			u.getAlerts();
			System.out.println("****** VERIFY AUTH, FOUND " + l.get(0) + " " + u.getAlerts());//.getAlerts().size());
			return l.get(0);
		} finally {
			if (doTransaction) dbManager.rollback();
		}
	}

	public static void authenticate(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		OAuthConsumer consumer = new DefaultOAuthConsumer(consumerKey, consumerSecret);
		OAuthProvider provider = new DefaultOAuthProvider(request_token_url, access_token_url, authorize_token_url);

		String url = req.getRequestURL().toString().replace("authenticate", "auth_callback");
		System.out.println("********* WANT TO SEND TO "+ url);
		String authUrl = provider.retrieveRequestToken(consumer, url);
		//req.getRequestURL().toString().replace("authenticate", ""));//callbackUrl);

		resp.addCookie(new Cookie("request_token", consumer.getToken()));
		resp.addCookie(new Cookie("request_token_secret", consumer.getTokenSecret()));
		resp.setStatus(302);
		resp.setHeader("Location", authUrl);
		return;
	}

	public static User processAuthReturn(DatabaseManager dbManager, HttpServletRequest req, HttpServletResponse resp) throws Exception {
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
		Document doc = XMLUtils.parse(osmResponse);
		Element e = (Element)XMLUtils.xpath(doc, "/osm/user").iterator().next();

		long osmId = Long.parseLong(e.getAttribute("id"));

		dbManager.getEM().getTransaction().begin();
		Query q = dbManager.getEM().createQuery("SELECT x FROM User x WHERE osmId= ?1");
		q.setParameter(1, osmId);

		User user = null;
		List<User> l = q.getResultList();
		if (l.size() > 0) {
			user = (User)l.get(0);
		} else {
			user = new User();
			user.setOsmId(osmId);
		}
		user.setScreenName(e.getAttribute("display_name"));

		UserSession us = new UserSession();
		us.setUser(user);
		us.setAccessToken(consumer.getToken());

		dbManager.getEM().persist(user);
		dbManager.getEM().persist(us);
		dbManager.getEM().getTransaction().commit();

		resp.addCookie(new Cookie("access_token", us.getAccessToken()));
		return user;
	}

	private static Logger logger = Logger.getLogger("osm.watch.auth");
}