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
    tableName = "sessions",
    foreignKeys = @ForeignKey(
        entity = UserEntity.class,
        parentColumns = "user_id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = "user_id"),
        @Index(value = "session_date"),
        @Index(value = {"user_id", "session_date"}, name = "idx_user_date"),
        @Index(value = "session_type"),
        @Index(value = "status")
    }
)
public class SessionEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "session_id")
    private int sessionId;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "session_date")
    @TypeConverters(DateConverter.class)
    private Date sessionDate;

    @ColumnInfo(name = "start_time")
    @TypeConverters(DateConverter.class)
    private Date startTime;

    @ColumnInfo(name = "end_time")
    @TypeConverters(DateConverter.class)
    private Date endTime;

    @ColumnInfo(name = "duration_minutes")
    private int durationMinutes;

    @ColumnInfo(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;

    @ColumnInfo(name = "session_type")
    private String sessionType;

    @ColumnInfo(name = "status", defaultValue = "IN_PROGRESS")
    private String status;

    @ColumnInfo(name = "focus_session_count", defaultValue = "0")
    private int focusSessionCount;

    @ColumnInfo(name = "is_completed", defaultValue = "0")
    private boolean isCompleted;

    @ColumnInfo(name = "pause_count", defaultValue = "0")
    private int pauseCount;

    @ColumnInfo(name = "total_pause_duration", defaultValue = "0")
    private int totalPauseDuration;

    @ColumnInfo(name = "created_at")
    @TypeConverters(DateConverter.class)
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    @TypeConverters(DateConverter.class)
    private Date updatedAt;

    // Constants
    public static final String TYPE_FOCUS_SESSION = "FOCUS_SESSION";
    public static final String TYPE_SHORT_BREAK = "SHORT_BREAK";
    public static final String TYPE_LONG_BREAK = "LONG_BREAK";

    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_PAUSED = "PAUSED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // Constructor
    public SessionEntity() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.status = STATUS_IN_PROGRESS;
    }

    // Getters and Setters
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public Date getSessionDate() { return sessionDate; }
    public void setSessionDate(Date sessionDate) { this.sessionDate = sessionDate; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { 
        this.durationMinutes = durationMinutes; 
    }

    public Integer getActualDurationMinutes() { return actualDurationMinutes; }
    public void setActualDurationMinutes(Integer actualDurationMinutes) { 
        this.actualDurationMinutes = actualDurationMinutes; 
    }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getFocusSessionCount() { return focusSessionCount; }
    public void setFocusSessionCount(int focusSessionCount) { 
        this.focusSessionCount = focusSessionCount; 
    }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getPauseCount() { return pauseCount; }
    public void setPauseCount(int pauseCount) { this.pauseCount = pauseCount; }

    public int getTotalPauseDuration() { return totalPauseDuration; }
    public void setTotalPauseDuration(int totalPauseDuration) { 
        this.totalPauseDuration = totalPauseDuration; 
    }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
