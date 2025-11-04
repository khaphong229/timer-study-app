package com.example.timerstudy.model;

import java.util.Date;

public class Task {
    private int task_id;
    private int user_id;
    private String title, description, priority;
    private Date task_date;
    private Boolean is_completed;
    private Date completed_at;
    private Integer total_time_spent, estimated_sessions, actual_sessions, order_index;
    private Date created_at, updated_at;

    public Task(int task_id, int user_id, String title, String description, String priority, Date task_date, Boolean is_completed, Date completed_at, Integer total_time_spent, Integer estimated_sessions, Integer actual_sessions, Integer order_index, Date created_at, Date updated_at) {
        this.task_id = task_id;
        this.user_id = user_id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.task_date = task_date;
        this.is_completed = is_completed;
        this.completed_at = completed_at;
        this.total_time_spent = total_time_spent;
        this.estimated_sessions = estimated_sessions;
        this.actual_sessions = actual_sessions;
        this.order_index = order_index;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getTask_id() {
        return task_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Date getTask_date() {
        return task_date;
    }

    public void setTask_date(Date task_date) {
        this.task_date = task_date;
    }

    public Boolean getIs_completed() {
        return is_completed;
    }

    public void setIs_completed(Boolean is_completed) {
        this.is_completed = is_completed;
    }

    public Date getCompleted_at() {
        return completed_at;
    }

    public void setCompleted_at(Date completed_at) {
        this.completed_at = completed_at;
    }

    public Integer getTotal_time_spent() {
        return total_time_spent;
    }

    public void setTotal_time_spent(Integer total_time_spent) {
        this.total_time_spent = total_time_spent;
    }

    public Integer getEstimated_sessions() {
        return estimated_sessions;
    }

    public void setEstimated_sessions(Integer estimated_sessions) {
        this.estimated_sessions = estimated_sessions;
    }

    public Integer getActual_sessions() {
        return actual_sessions;
    }

    public void setActual_sessions(Integer actual_sessions) {
        this.actual_sessions = actual_sessions;
    }

    public Integer getOrder_index() {
        return order_index;
    }

    public void setOrder_index(Integer order_index) {
        this.order_index = order_index;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }
}
