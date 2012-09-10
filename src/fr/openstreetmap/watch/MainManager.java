package fr.openstreetmap.watch;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class MainManager {
    EntityManager em;
    public void init() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PU");
        em = emf.createEntityManager();
    }
    
    public void addAlert(AlertDesc ad) {
        em.getTransaction().begin();
        em.persist(ad);
        em.getTransaction().commit();
    }
}
