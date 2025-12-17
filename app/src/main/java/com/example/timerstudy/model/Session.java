package com.example.timerstudy.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Session implements Serializable {
    
    private int session_id;
    private long user_id;

    @SerializedName("session_date")
    private Long session_date;

    @SerializedName("start_time")
    private Long start_time;

    @SerializedName("end_time")
    private Long end_time;

    @SerializedName("duration_minutes")
    private Integer duration_minutes;

    @SerializedName("actual_duration_minutes")
    private Integer actual_duration_minutes;

    @SerializedName("session_type")
    private String session_type;

    @SerializedName("status")
    private String status;

    @SerializedName("focus_session_count")
    private Integer focus_session_count;

    @SerializedName("is_completed")
    private Integer is_completed;

    @SerializedName("pause_count")
    private Integer pause_count;

    @SerializedName("total_pause_duration")
    private Integer total_pause_duration;

    private String notes;
    
    private String created_at; 

    public Session() {
    }

    public Session(int session_id, long user_id, Long session_date, Long start_time, Long end_time, 
                   Integer duration_minutes, Integer actual_duration_minutes, String session_type, 
                   String status, Integer focus_session_count, Integer is_completed, 
                   Integer pause_count, Integer total_pause_duration, String notes) {
        this.session_id = session_id;
        this.user_id = user_id;
        this.session_date = session_date;
        this.start_time = start_time;
        this.end_time = end_time;
        this.duration_minutes = duration_minutes;
        this.actual_duration_minutes = actual_duration_minutes;
        this.session_type = session_type;
        this.status = status;
        this.focus_session_count = focus_session_count;
        this.is_completed = is_completed;
        this.pause_count = pause_count;
        this.total_pause_duration = total_pause_duration;
        this.notes = notes;
    }

    public int getSession_id() {
        return session_id;
    }

    public void setSession_id(int session_id) {
        this.session_id = session_id;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public Long getSession_date() {
        return session_date;
    }

    public void setSession_date(Long session_date) {
        this.session_date = session_date;
    }

    public Long getStart_time() {
        return start_time;
    }

    public void setStart_time(Long start_time) {
        this.start_time = start_time;
    }

    public Long getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Long end_time) {
        this.end_time = end_time;
    }

    public Integer getDuration_minutes() {
        return duration_minutes;
    }

    public void setDuration_minutes(Integer duration_minutes) {
        this.duration_minutes = duration_minutes;
    }

    public Integer getActual_duration_minutes() {
        return actual_duration_minutes;
    }

    public void setActual_duration_minutes(Integer actual_duration_minutes) {
        this.actual_duration_minutes = actual_duration_minutes;
    }

    public String getSession_type() {
        return session_type;
    }

    public void setSession_type(String session_type) {
        this.session_type = session_type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getFocus_session_count() {
        return focus_session_count;
    }

    public void setFocus_session_count(Integer focus_session_count) {
        this.focus_session_count = focus_session_count;
    }

    public Integer getIs_completed() {
        return is_completed;
    }

    public void setIs_completed(Integer is_completed) {
        this.is_completed = is_completed;
    }

    public Integer getPause_count() {
        return pause_count;
    }

    public void setPause_count(Integer pause_count) {
        this.pause_count = pause_count;
    }

    public Integer getTotal_pause_duration() {
        return total_pause_duration;
    }

    public void setTotal_pause_duration(Integer total_pause_duration) {
        this.total_pause_duration = total_pause_duration;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
