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
    tableName = "statistics_cache",
    foreignKeys = @ForeignKey(
        entity = UserEntity.class,
        parentColumns = "user_id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = "user_id"),
        @Index(value = "cache_date"),
        @Index(value = {"user_id", "cache_date", "cache_type"}, 
               unique = true, name = "unique_user_cache")
    }
)
public class StatisticsCacheEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "cache_id")
    private int cacheId;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "cache_date")
    @TypeConverters(DateConverter.class)
    private Date cacheDate;

    @ColumnInfo(name = "cache_type")
    private String cacheType;

    @ColumnInfo(name = "total_sessions", defaultValue = "0")
    private int totalSessions;

    @ColumnInfo(name = "total_focus_time", defaultValue = "0")
    private int totalFocusTime;

    @ColumnInfo(name = "total_break_time", defaultValue = "0")
    private int totalBreakTime;

    @ColumnInfo(name = "completed_tasks", defaultValue = "0")
    private int completedTasks;

    @ColumnInfo(name = "goal_achieved", defaultValue = "0")
    private boolean goalAchieved;

    @ColumnInfo(name = "current_streak", defaultValue = "0")
    private int currentStreak;

    @ColumnInfo(name = "best_streak", defaultValue = "0")
    private int bestStreak;

    @ColumnInfo(name = "cached_at")
    @TypeConverters(DateConverter.class)
    private Date cachedAt;

    // Constants
    public static final String TYPE_DAILY = "DAILY";
    public static final String TYPE_MONTHLY = "MONTHLY";
    public static final String TYPE_YEARLY = "YEARLY";

    // Constructor
    public StatisticsCacheEntity() {
        this.cachedAt = new Date();
    }

    // Getters and Setters
    public int getCacheId() { return cacheId; }
    public void setCacheId(int cacheId) { this.cacheId = cacheId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Date getCacheDate() { return cacheDate; }
    public void setCacheDate(Date cacheDate) { this.cacheDate = cacheDate; }

    public String getCacheType() { return cacheType; }
    public void setCacheType(String cacheType) { this.cacheType = cacheType; }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { 
        this.totalSessions = totalSessions; 
    }

    public int getTotalFocusTime() { return totalFocusTime; }
    public void setTotalFocusTime(int totalFocusTime) { 
        this.totalFocusTime = totalFocusTime; 
    }

    public int getTotalBreakTime() { return totalBreakTime; }
    public void setTotalBreakTime(int totalBreakTime) { 
        this.totalBreakTime = totalBreakTime; 
    }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { 
        this.completedTasks = completedTasks; 
    }

    public boolean isGoalAchieved() { return goalAchieved; }
    public void setGoalAchieved(boolean goalAchieved) { 
        this.goalAchieved = goalAchieved; 
    }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { 
        this.currentStreak = currentStreak; 
    }

    public int getBestStreak() { return bestStreak; }
    public void setBestStreak(int bestStreak) { this.bestStreak = bestStreak; }

    public Date getCachedAt() { return cachedAt; }
    public void setCachedAt(Date cachedAt) { this.cachedAt = cachedAt; }
}
