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
    tableName = "session_pauses",
    foreignKeys = @ForeignKey(
        entity = SessionEntity.class,
        parentColumns = "session_id",
        childColumns = "session_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index(value = "session_id")}
)
public class SessionPauseEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pause_id")
    private int pauseId;

    @ColumnInfo(name = "session_id")
    private int sessionId;

    @ColumnInfo(name = "pause_start")
    @TypeConverters(DateConverter.class)
    private Date pauseStart;

    @ColumnInfo(name = "pause_end")
    @TypeConverters(DateConverter.class)
    private Date pauseEnd;

    @ColumnInfo(name = "pause_duration")
    private Integer pauseDuration;

    // Getters and Setters
    public int getPauseId() { return pauseId; }
    public void setPauseId(int pauseId) { this.pauseId = pauseId; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public Date getPauseStart() { return pauseStart; }
    public void setPauseStart(Date pauseStart) { this.pauseStart = pauseStart; }

    public Date getPauseEnd() { return pauseEnd; }
    public void setPauseEnd(Date pauseEnd) { this.pauseEnd = pauseEnd; }

    public Integer getPauseDuration() { return pauseDuration; }
    public void setPauseDuration(Integer pauseDuration) { 
        this.pauseDuration = pauseDuration; 
    }
}
