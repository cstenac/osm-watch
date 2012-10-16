package fr.openstreetmap.watch.model.db;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name="OSMWUser")
public class User {
    @Id
    private long osmId;
    private String screenName;
    private String emailAddress;
    
    @OneToMany(mappedBy="user", fetch= FetchType.LAZY)
    private Set<UserSession> sessions;
    @OneToMany(mappedBy="user", fetch= FetchType.LAZY)
    private Set<Alert> alerts;
    
    public Set<Alert> getAlerts() {
        return alerts;
    }
    public void setAlerts(Set<Alert> alerts) {
        this.alerts = alerts;
    }
    public long getOsmId() {
        return osmId;
    }
    public void setOsmId(long osmId) {
        this.osmId = osmId;
    }
    public String getScreenName() {
        return screenName;
    }
    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }
    public String getEmailAddress() {
        return emailAddress;
    }
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
	public Set<UserSession> getSessions() {
		return sessions;
	}
	public void setSessions(Set<UserSession> sessions) {
		this.sessions = sessions;
	}
}
