package fr.openstreetmap.watch.model.db;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Entity
public class ApplicationState {
    private long lastDownloadedId;

    public long getLastDownloadedId() {
        return lastDownloadedId;
    }

    public void setLastDownloadedId(long lastDownloadedId) {
        this.lastDownloadedId = lastDownloadedId;
    }
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
