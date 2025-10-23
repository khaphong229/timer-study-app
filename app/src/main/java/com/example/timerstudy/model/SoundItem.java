package com.example.timerstudy.model;

public class SoundItem {
    private String name;
    private int resourceId;
    private boolean isPlaying;
    private float volume;

    public SoundItem(String name, int resourceId, boolean isPlaying, float volume) {
        this.name = name;
        this.resourceId = resourceId;
        this.isPlaying = isPlaying;
        this.volume = volume;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }
}
