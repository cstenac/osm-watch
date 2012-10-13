package fr.openstreetmap.watch;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import fr.openstreetmap.watch.model.db.Alert;

@Service
public class DatabaseManager {
    EntityManagerFactory emf;
    ThreadLocal<EntityManager> emtl = new ThreadLocal<EntityManager>();
    
    @PostConstruct
    public void init() {
        emf = Persistence.createEntityManagerFactory("PU");
    }
    
    public void begin() {
    	logger.info("Begin on EM" + getEM());
    	getEM().getTransaction().begin();
    }
    public void commit() {
    	logger.info("Commit on EM" + getEM());
    	getEM().getTransaction().commit();
    	getEM().close();
    	emtl.remove();
    }
    public void rollback() {
    	logger.info("Rollback on EM" + getEM());

    	getEM().getTransaction().rollback();
    	getEM().close();
    	emtl.remove();
    }
    
    public void addAlert(Alert ad) {
        getEM().persist(ad);
    }
    
    public EntityManager getEM() {
    	if (emtl.get() == null) {
    		logger.info("Create an EM !");
    		emtl.set(emf.createEntityManager());
    	}
        return emtl.get();
    }
    
    @SuppressWarnings("unchecked")
	public List<Alert> getAlerts() {
        Query q = getEM().createQuery ("SELECT x FROM Alert x");
        return (List<Alert>) q.getResultList ();
    }
    
    public Alert getAlertByKey(String uniqueKey) {
        Query q = getEM().createQuery("SELECT x FROM Alert x where uniqueKey = ?1");
        q.setParameter(1, uniqueKey);
        @SuppressWarnings("unchecked")
		List<Alert> aa = q.getResultList();
        if (aa.size() == 0) {
            return null;
        }
        return aa.get(0);
    }
    
    public void deleteAlert(String uniqueKey) {
    	logger.info("Delete on EM" + getEM());

        Query q = getEM().createQuery ("SELECT x FROM Alert x WHERE x.uniqueKey = ?1");
        q.setParameter (1, uniqueKey);
        @SuppressWarnings("unchecked")
		List<Alert> results = (List<Alert>) q.getResultList ();
        logger.info("Removing alert " + uniqueKey +" -> " + results.size() + " matches");
        for (Alert ar : results) {
        	getEM().remove(ar);
        }
    }
    
    private static Logger logger = Logger.getLogger("osm.watch.database");
}
