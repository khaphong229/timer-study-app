package com.example.timerstudy.model;

public class TimerItem {
    private int id;
    private String name;
    private long totalTime;
    private long currentTime;
    private boolean isRunning;
    
    public TimerItem(String name, long totalSeconds) {
        this.name = name;
        this.totalTime = totalSeconds;
        this.currentTime = totalSeconds;
        this.isRunning = false;
    }
    
    public TimerItem(int id, String name, long totalTime, long currentTime, boolean isRunning) {
        this.id = id;
        this.name = name;
        this.totalTime = totalTime;
        this.currentTime = currentTime;
        this.isRunning = isRunning;
    }
    
    public void tick() {
        if (currentTime > 0) {
            currentTime--;
        }
    }
    
    public void reset() {
        currentTime = totalTime;
        isRunning = false;
    }
    
    public String getFormattedTime() {
        long hours = currentTime / 3600;
        long minutes = (currentTime % 3600) / 60;
        long seconds = currentTime % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public long getTotalTime() { return totalTime; }
    public void setTotalTime(long totalTime) { this.totalTime = totalTime; }
    
    public long getCurrentTime() { return currentTime; }
    public void setCurrentTime(long currentTime) { this.currentTime = currentTime; }
    
    public boolean isRunning() { return isRunning; }
    public void setRunning(boolean running) { isRunning = running; }
}
