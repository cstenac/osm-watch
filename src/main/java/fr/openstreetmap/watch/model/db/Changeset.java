package fr.openstreetmap.watch.model.db;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class Changeset {
  public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    
    public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public long getUid() {
		return uid;
	}
	public void setUid(long uid) {
		this.uid = uid;
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
	public int getNewNodes() {
		return newNodes;
	}
	public void setNewNodes(int newNodes) {
		this.newNodes = newNodes;
	}
	public int getDeletedNodes() {
		return deletedNodes;
	}
	public void setDeletedNodes(int deletedNodes) {
		this.deletedNodes = deletedNodes;
	}
	public int getChangedNodes() {
		return changedNodes;
	}
	public void setChangedNodes(int changedNodes) {
		this.changedNodes = changedNodes;
	}
	public int getNewWays() {
		return newWays;
	}
	public void setNewWays(int newWays) {
		this.newWays = newWays;
	}
	public int getDeletedWays() {
		return deletedWays;
	}
	public void setDeletedWays(int deletedWays) {
		this.deletedWays = deletedWays;
	}
	public int getChangedWays() {
		return changedWays;
	}
	public void setChangedWays(int changedWays) {
		this.changedWays = changedWays;
	}
	public int getWaysWithChangedNodes() {
		return waysWithChangedNodes;
	}
	public void setWaysWithChangedNodes(int waysWithChangedNodes) {
		this.waysWithChangedNodes = waysWithChangedNodes;
	}

	@Id
    private long id;

    private long timestamp = System.currentTimeMillis();
    private String userName;
    private long uid;

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    private int newNodes;
    private int deletedNodes;
    private int changedNodes;
    
    private int newWays;
    private int deletedWays;
    private int changedWays;
    private int waysWithChangedNodes;

}
