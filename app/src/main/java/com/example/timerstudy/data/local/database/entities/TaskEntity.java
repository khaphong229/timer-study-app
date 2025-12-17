package com.example.timerstudy.data.local.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.timerstudy.data.local.database.converters.DateConverter;

import java.util.Date;

@Entity(
    tableName = "tasks",
    foreignKeys = @ForeignKey(
        entity = UserEntity.class,
        parentColumns = "user_id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = "user_id"),
        @Index(value = "task_date"),
        @Index(value = {"user_id", "task_date"}, name = "idx_user_task_date"),
        @Index(value = "priority"),
        @Index(value = "is_completed")
    }
)
public class TaskEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "task_id")
    private int taskId;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "priority", defaultValue = "MEDIUM")
    private String priority;

    @ColumnInfo(name = "task_date")
    @TypeConverters(DateConverter.class)
    private Date taskDate;

    @ColumnInfo(name = "is_completed", defaultValue = "0")
    private boolean isCompleted;

    @ColumnInfo(name = "completed_at")
    @TypeConverters(DateConverter.class)
    private Date completedAt;

    @ColumnInfo(name = "total_time_spent", defaultValue = "0")
    private int totalTimeSpent;

    @ColumnInfo(name = "estimated_sessions", defaultValue = "1")
    private int estimatedSessions;

    @ColumnInfo(name = "actual_sessions", defaultValue = "0")
    private int actualSessions;

    @ColumnInfo(name = "order_index", defaultValue = "0")
    private int orderIndex;

    @ColumnInfo(name = "created_at")
    @TypeConverters(DateConverter.class)
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    @TypeConverters(DateConverter.class)
    private Date updatedAt;

    // Constants
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_LOW = "LOW";

    // Constructor
    public TaskEntity() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.priority = PRIORITY_MEDIUM;
    }

    // Getters and Setters
    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Date getTaskDate() { return taskDate; }
    public void setTaskDate(Date taskDate) { this.taskDate = taskDate; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }

    public int getTotalTimeSpent() { return totalTimeSpent; }
    public void setTotalTimeSpent(int totalTimeSpent) { 
        this.totalTimeSpent = totalTimeSpent; 
    }

    public int getEstimatedSessions() { return estimatedSessions; }
    public void setEstimatedSessions(int estimatedSessions) { 
        this.estimatedSessions = estimatedSessions; 
    }

    public int getActualSessions() { return actualSessions; }
    public void setActualSessions(int actualSessions) { 
        this.actualSessions = actualSessions; 
    }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
