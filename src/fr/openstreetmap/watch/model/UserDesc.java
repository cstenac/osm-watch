package fr.openstreetmap.watch.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class UserDesc {
    @Id
    private long osmId;
    private String screenName;
    private String emailAddress;
    private String accessToken;
    private String accessTokenSecret;
    
    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }
    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }
    @OneToMany(mappedBy="user")
    private Set<AlertDesc> alerts;
    
    public Set<AlertDesc> getAlerts() {
        return alerts;
    }
    public void setAlerts(Set<AlertDesc> alerts) {
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
}
