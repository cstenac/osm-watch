package fr.openstreetmap.watch.model.db;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;


@Entity
public class Alert {
  public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getUniqueKey() {
        return uniqueKey;
    }
    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }
    public String getWatchedTags() {
        return watchedTags;
    }
    public void setWatchedTags(String watchedTags) {
        this.watchedTags = watchedTags;
    }
    public String getPolygonWKT() {
        return polygonWKT;
    }
    public void setPolygonWKT(String polygonWKT) {
        this.polygonWKT = polygonWKT;
    }
    
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public Set<AlertMatch> getAlertMatches() {
		return alertMatches;
	}
	public void setAlertMatches(Set<AlertMatch> alertMatches) {
		this.alertMatches = alertMatches;
	}


	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;

    @ManyToOne(fetch=FetchType.LAZY)
    private User user;
    
    @OneToMany(mappedBy="alert", fetch= FetchType.LAZY)
    private Set<AlertMatch> alertMatches;
    
    private String uniqueKey;
    private String watchedTags;
    private String polygonWKT;
}
