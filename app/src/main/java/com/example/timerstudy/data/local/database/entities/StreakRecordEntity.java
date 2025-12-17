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
    tableName = "streak_records",
    foreignKeys = @ForeignKey(
        entity = UserEntity.class,
        parentColumns = "user_id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = "user_id"),
        @Index(value = "streak_date"),
        @Index(value = {"user_id", "streak_date"}, 
               unique = true, name = "unique_user_streak_date")
    }
)
public class StreakRecordEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "streak_id")
    private int streakId;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "streak_date")
    @TypeConverters(DateConverter.class)
    private Date streakDate;

    @ColumnInfo(name = "has_activity", defaultValue = "0")
    private boolean hasActivity;

    @ColumnInfo(name = "session_count", defaultValue = "0")
    private int sessionCount;

    @ColumnInfo(name = "focus_time", defaultValue = "0")
    private int focusTime;

    // Getters and Setters
    public int getStreakId() { return streakId; }
    public void setStreakId(int streakId) { this.streakId = streakId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public Date getStreakDate() { return streakDate; }
    public void setStreakDate(Date streakDate) { this.streakDate = streakDate; }

    public boolean isHasActivity() { return hasActivity; }
    public void setHasActivity(boolean hasActivity) { this.hasActivity = hasActivity; }

    public int getSessionCount() { return sessionCount; }
    public void setSessionCount(int sessionCount) { this.sessionCount = sessionCount; }

    public int getFocusTime() { return focusTime; }
    public void setFocusTime(int focusTime) { this.focusTime = focusTime; }
}
