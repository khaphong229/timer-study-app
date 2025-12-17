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
    tableName = "goals",
    foreignKeys = @ForeignKey(
        entity = UserEntity.class,
        parentColumns = "user_id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = "user_id"),
        @Index(value = "goal_date"),
        @Index(value = {"user_id", "goal_date"}, name = "unique_user_goal_date")
    }
)
public class GoalEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "goal_id")
    private int goalId;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "goal_date")
    @TypeConverters(DateConverter.class)
    private Date goalDate;

    @ColumnInfo(name = "target_sessions")
    private int targetSessions;

    @ColumnInfo(name = "completed_sessions", defaultValue = "0")
    private int completedSessions;

    @ColumnInfo(name = "completion_percentage", defaultValue = "0")
    private int completionPercentage;

    @ColumnInfo(name = "is_achieved", defaultValue = "0")
    private boolean isAchieved;

    @ColumnInfo(name = "achieved_at")
    @TypeConverters(DateConverter.class)
    private Date achievedAt;

    @ColumnInfo(name = "created_at")
    @TypeConverters(DateConverter.class)
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    @TypeConverters(DateConverter.class)
    private Date updatedAt;

    // Constructor
    public GoalEntity() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters and Setters
    public int getGoalId() { return goalId; }
    public void setGoalId(int goalId) { this.goalId = goalId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public Date getGoalDate() { return goalDate; }
    public void setGoalDate(Date goalDate) { this.goalDate = goalDate; }

    public int getTargetSessions() { return targetSessions; }
    public void setTargetSessions(int targetSessions) { 
        this.targetSessions = targetSessions; 
    }

    public int getCompletedSessions() { return completedSessions; }
    public void setCompletedSessions(int completedSessions) { 
        this.completedSessions = completedSessions; 
    }

    public int getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(int completionPercentage) { 
        this.completionPercentage = completionPercentage; 
    }

    public boolean isAchieved() { return isAchieved; }
    public void setAchieved(boolean achieved) { isAchieved = achieved; }

    public Date getAchievedAt() { return achievedAt; }
    public void setAchievedAt(Date achievedAt) { this.achievedAt = achievedAt; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
