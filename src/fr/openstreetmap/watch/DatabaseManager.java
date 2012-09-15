package fr.openstreetmap.watch;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import fr.openstreetmap.watch.model.db.AlertDesc;

@Service
public class DatabaseManager {
    EntityManager em;
    
    @PostConstruct
    public void init() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PU");
        em = emf.createEntityManager();
    }
    
    public void addAlert(AlertDesc ad) {
        em.getTransaction().begin();
        em.persist(ad);
        em.getTransaction().commit();
    }
    
    public EntityManager getEM() {
        return em;
    }
    
    public List<AlertDesc> getAlerts() {
        Query q = em.createQuery ("SELECT x FROM AlertDesc x");
        return (List<AlertDesc>) q.getResultList ();
    }
    
    public void deleteAlert(String uniqueKey) {
        em.getTransaction().begin();
        
        Query q = em.createQuery ("SELECT x FROM AlertDesc x WHERE x.uniqueKey = ?1");
        q.setParameter (1, uniqueKey);
        List<AlertDesc> results = (List<AlertDesc>) q.getResultList ();
        logger.info("Removing alert " + uniqueKey +" -> " + results.size() + " matches");
        for (AlertDesc ar : results) {
            em.remove(ar);
        }
        em.getTransaction().commit();
    }
    
    private static Logger logger = Logger.getLogger("osm.watch.database");
}
