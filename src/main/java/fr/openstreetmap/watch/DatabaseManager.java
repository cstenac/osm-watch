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
    EntityManager em;
    
    @PostConstruct
    public void init() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PU");
        em = emf.createEntityManager();
    }
    
    public void begin() {
    	em.getTransaction().begin();
    }
    public void commit() {
    	em.getTransaction().commit();
    }
    public void rollback() {
    	em.getTransaction().rollback();
    }
    
    public void addAlert(Alert ad) {
        em.persist(ad);
    }
    
    public EntityManager getEM() {
        return em;
    }
    
    @SuppressWarnings("unchecked")
	public List<Alert> getAlerts() {
        Query q = em.createQuery ("SELECT x FROM Alert x");
        return (List<Alert>) q.getResultList ();
    }
    
    public Alert getAlertByKey(String uniqueKey) {
        Query q = em.createQuery("SELECT x FROM Alert x where uniqueKey = ?1");
        q.setParameter(1, uniqueKey);
        @SuppressWarnings("unchecked")
		List<Alert> aa = q.getResultList();
        if (aa.size() == 0) {
            return null;
        }
        return aa.get(0);
    }
    
    public void deleteAlert(String uniqueKey) {
        Query q = em.createQuery ("SELECT x FROM Alert x WHERE x.uniqueKey = ?1");
        q.setParameter (1, uniqueKey);
        @SuppressWarnings("unchecked")
		List<Alert> results = (List<Alert>) q.getResultList ();
        logger.info("Removing alert " + uniqueKey +" -> " + results.size() + " matches");
        for (Alert ar : results) {
            em.remove(ar);
        }
    }
    
    private static Logger logger = Logger.getLogger("osm.watch.database");
}
