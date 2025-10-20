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
    tableName = "task_sessions",
    foreignKeys = {
        @ForeignKey(
            entity = TaskEntity.class,
            parentColumns = "task_id",
            childColumns = "task_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = SessionEntity.class,
            parentColumns = "session_id",
            childColumns = "session_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = "task_id"),
        @Index(value = "session_id"),
        @Index(value = {"task_id", "session_id"}, name = "idx_task_session")
    }
)
public class TaskSessionEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "task_session_id")
    private int taskSessionId;

    @ColumnInfo(name = "task_id")
    private int taskId;

    @ColumnInfo(name = "session_id")
    private int sessionId;

    @ColumnInfo(name = "time_spent")
    private int timeSpent;

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "created_at")
    @TypeConverters(DateConverter.class)
    private Date createdAt;

    // Constructor
    public TaskSessionEntity() {
        this.createdAt = new Date();
    }

    // Getters and Setters
    public int getTaskSessionId() { return taskSessionId; }
    public void setTaskSessionId(int taskSessionId) { 
        this.taskSessionId = taskSessionId; 
    }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getTimeSpent() { return timeSpent; }
    public void setTimeSpent(int timeSpent) { this.timeSpent = timeSpent; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
