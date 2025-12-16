package com.example.timerstudy.model;

import java.util.Date;

public class Session {
    private int session_id;
    private long user_id;
    private Date session_date;
    private Date start_time;
    private Date end_time;
    private int duration_minutes;
    private String session_type;
    private String status;
    private String notes;
    private Date created_at;

    public Session() {
    }

    public Session(int session_id, long user_id, Date session_date, Date start_time, Date end_time, int duration_minutes, String session_type, String status, String notes, Date created_at) {
        this.session_id = session_id;
        this.user_id = user_id;
        this.session_date = session_date;
        this.start_time = start_time;
        this.end_time = end_time;
        this.duration_minutes = duration_minutes;
        this.session_type = session_type;
        this.status = status;
        this.notes = notes;
        this.created_at = created_at;
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

    public Date getSession_date() {
        return session_date;
    }

    public void setSession_date(Date session_date) {
        this.session_date = session_date;
    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

    public int getDuration_minutes() {
        return duration_minutes;
    }

    public void setDuration_minutes(int duration_minutes) {
        this.duration_minutes = duration_minutes;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }
}
