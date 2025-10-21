package com.example.timerstudy.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "timers")
public class TimerEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String name;
    private long duration; // in milliseconds
    private long remainingTime;
    private boolean isActive;
    private long createdAt;
    
    public TimerEntity(String name, long duration) {
        this.name = name;
        this.duration = duration;
        this.remainingTime = duration;
        this.isActive = false;
        this.createdAt = System.currentTimeMillis();
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    
    public long getRemainingTime() { return remainingTime; }
    public void setRemainingTime(long remainingTime) { this.remainingTime = remainingTime; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
