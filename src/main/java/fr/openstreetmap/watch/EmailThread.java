package fr.openstreetmap.watch;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
	public void init() {
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
		
	private void sendMail(Alert a) {
		String name = a.getUser().getScreenName();
		String address = a.getUser().getEmailAddress();
		logger.info("Sending mail to " + name + "<" + address + "> about " + a.getName());
		
		Query q = dbManager.getEM().createQuery(
				"SELECT am from AlertMatch am where am.alert=?1 AND am.matchTimestamp > ?2");
		q.setParameter(1, a);
		q.setParameter(2, a.getLastEmailTimestamp());

		List<AlertMatch> matches = q.getResultList();
		logger.info("Have "  +matches.size() + " matches");
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
