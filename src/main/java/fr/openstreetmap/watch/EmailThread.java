package fr.openstreetmap.watch;

import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.openstreetmap.watch.model.db.Alert;
import fr.openstreetmap.watch.model.db.AlertMatch;

@Service
public class EmailThread {
	@Autowired private DatabaseManager dbManager;

	volatile boolean stopping = false;
	Thread t;

	@PostConstruct
	public void pc() {
		t = new BGThread();
		t.start();
	}

	@PreDestroy
	public void uninit() throws InterruptedException {
		logger.info("Uninitializing email thread");
		stopping = true;
		t.notifyAll();
		t.join();
	}

	private void sendMail(Alert a) throws Exception {
		String name = a.getUser().getScreenName();
		String address = a.getUser().getEmailAddress();
		logger.info("Sending mail to " + name + "<" + address + "> about " + a.getName());
		if (address == null) {
			logger.warn("Not sending it ! address not given !");
			return;
		}

		Query q = dbManager.getEM().createQuery(
				"SELECT am from AlertMatch am where am.alert=?1 AND am.matchTimestamp > ?2");
		q.setParameter(1, a);
		q.setParameter(2, a.getLastEmailTimestamp());

		List<AlertMatch> matches = q.getResultList();
		logger.info("Have "  +matches.size() + " matches");

		Properties props = System.getProperties();
		props.put("mail.smtp.host", ApplicationConfigurator.getMandatoryProperty("mail.smtp.host"));
		Session session = Session.getDefaultInstance(props, null);

		Message msg = new MimeMessage(session);
		msg.setSubject("[osm-watch] Notification for: " + a.getName());
		msg.setFrom(new InternetAddress("OSM-Watch <no-reply@openstreetmap.fr>"));
		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(address, false));

		StringBuilder content = new StringBuilder();

		content.append("<p>Dear " + name + ",</p>");
		content.append("<p>Here are the latest changesets matching your alert '" + name + "'</p>");

		content.append("<ul>");
		for (AlertMatch am : matches) {
			content.append("<li>");
			content.append("<a href=\"http://openstreetmap.org/browse/changeset/" + am.getChangesetId() + "\">Changeset " + am.getChangesetId() + "</a><br />");
			content.append("</li>");
		}
		content.append("</ul>");

		content.append("More details in the <a href=\"" + ApplicationConfigurator.getBaseURL() + "/api/rss_feed?key=" + a.getUniqueKey() + "\">RSS feed</a>");

		msg.setContent(content.toString(), "text/html; charset=utf-8");
		Transport.send(msg);
	}

	class BGThread extends Thread {
		public void run() {
			logger.info("Starting email background thread");
			while (!stopping) {

				dbManager.begin();
				try {
					Query q = dbManager.getEM().createQuery(
							"SELECT a from Alert a WHERE a.emailEnabled=true AND a.lastEmailTimestamp <?1");
					q.setParameter(1, System.currentTimeMillis() - 86400*1000);

					List<Alert> alerts = q.getResultList();

					logger.info("Email background thread has " + alerts.size() + " mails to send");

					for (Alert a : alerts) {
						sendMail(a);
						a.setLastEmailTimestamp(System.currentTimeMillis());
						dbManager.getEM().persist(a);
					}

					dbManager.commit();
				}catch (Throwable t){
					dbManager.rollback();
					logger.error("Failed to run email run", t);
				}

				try {
					synchronized (this) {
						wait(60000);
					}
				} catch (InterruptedException e) {
				}
			}
		}
	}
	private static Logger logger = Logger.getLogger("osm.watch.engine");
}
