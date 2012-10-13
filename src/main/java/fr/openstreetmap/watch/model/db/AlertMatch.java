package fr.openstreetmap.watch.model.db;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * A changeset that matches an alert
 */
@Entity
public class AlertMatch {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    private Alert alert;
    
    private long matchTimestamp;
    private long changesetId;
    
    private String reason;
    
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Alert getAlert() {
		return alert;
	}

	public void setAlert(Alert alert) {
		this.alert = alert;
	}

	public long getMatchTimestamp() {
		return matchTimestamp;
	}

	public void setMatchTimestamp(long matchTimestamp) {
		this.matchTimestamp = matchTimestamp;
	}

	public long getChangesetId() {
		return changesetId;
	}

	public void setChangesetId(long changesetId) {
		this.changesetId = changesetId;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public double getMinX() {
		return minX;
	}

	public void setMinX(double minX) {
		this.minX = minX;
	}

	public double getMaxX() {
		return maxX;
	}

	public void setMaxX(double maxX) {
		this.maxX = maxX;
	}

	public double getMinY() {
		return minY;
	}

	public void setMinY(double minY) {
		this.minY = minY;
	}

	public double getMaxY() {
		return maxY;
	}

	public void setMaxY(double maxY) {
		this.maxY = maxY;
	}
}