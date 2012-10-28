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
    public String getFilterClass() {
		return filterClass;
	}
	public void setFilterClass(String filterClass) {
		this.filterClass = filterClass;
	}
	public String getFilterParams() {
        return filterParams;
    }
    public void setFilterParams(String filterParams) {
        this.filterParams = filterParams;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getCreationTimestamp() {
		return creationTimestamp;
	}
	public void setCreationTimestamp(long creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}
	public boolean isPublicAlert() {
        return publicAlert;
    }
    public void setPublicAlert(boolean publicAlert) {
        this.publicAlert = publicAlert;
    }
    public boolean isEmailEnabled() {
		return emailEnabled;
	}
	public void setEmailEnabled(boolean emailEnabled) {
		this.emailEnabled = emailEnabled;
	}
	public long getLastEmailTimestamp() {
		return lastEmailTimestamp;
	}
	public void setLastEmailTimestamp(long lastEmailTimestamp) {
		this.lastEmailTimestamp = lastEmailTimestamp;
	}


	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;

    @ManyToOne(fetch=FetchType.LAZY)
    private User user;
    
    @OneToMany(mappedBy="alert", fetch= FetchType.LAZY)
    private Set<AlertMatch> alertMatches;
    
    private long creationTimestamp = System.currentTimeMillis();
    private String name;
    private String uniqueKey;
    private String filterClass;
    private String filterParams;
    private String polygonWKT;
    private boolean publicAlert;
    private boolean emailEnabled;
    private long lastEmailTimestamp;
}
