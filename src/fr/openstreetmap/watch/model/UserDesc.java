package fr.openstreetmap.watch.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
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
    @OneToMany(mappedBy="user", fetch= FetchType.LAZY)
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
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessToken == null) ? 0 : accessToken.hashCode());
        result = prime * result + ((accessTokenSecret == null) ? 0 : accessTokenSecret.hashCode());
        result = prime * result + ((alerts == null) ? 0 : alerts.hashCode());
        result = prime * result + ((emailAddress == null) ? 0 : emailAddress.hashCode());
        result = prime * result + (int) (osmId ^ (osmId >>> 32));
        result = prime * result + ((screenName == null) ? 0 : screenName.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        UserDesc other = (UserDesc) obj;
        if (accessToken == null) {
            if (other.accessToken != null) return false;
        } else if (!accessToken.equals(other.accessToken)) return false;
        if (accessTokenSecret == null) {
            if (other.accessTokenSecret != null) return false;
        } else if (!accessTokenSecret.equals(other.accessTokenSecret)) return false;
        if (alerts == null) {
            if (other.alerts != null) return false;
        } else if (!alerts.equals(other.alerts)) return false;
        if (emailAddress == null) {
            if (other.emailAddress != null) return false;
        } else if (!emailAddress.equals(other.emailAddress)) return false;
        if (osmId != other.osmId) return false;
        if (screenName == null) {
            if (other.screenName != null) return false;
        } else if (!screenName.equals(other.screenName)) return false;
        return true;
    }
}
