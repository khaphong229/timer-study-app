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
    tableName = "user_settings",
    foreignKeys = @ForeignKey(
        entity = UserEntity.class,
        parentColumns = "user_id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = "user_id"),
        @Index(value = {"user_id", "setting_key"}, unique = true, name = "unique_user_setting")
    }
)
public class UserSettingEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "setting_id")
    private int settingId;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "setting_key")
    private String settingKey;

    @ColumnInfo(name = "setting_value")
    private String settingValue;

    @ColumnInfo(name = "data_type", defaultValue = "STRING")
    private String dataType;

    @ColumnInfo(name = "created_at")
    @TypeConverters(DateConverter.class)
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    @TypeConverters(DateConverter.class)
    private Date updatedAt;

    // Constants
    public static final String TYPE_STRING = "STRING";
    public static final String TYPE_INTEGER = "INTEGER";
    public static final String TYPE_BOOLEAN = "BOOLEAN";
    public static final String TYPE_JSON = "JSON";

    // Constructor
    public UserSettingEntity() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.dataType = TYPE_STRING;
    }

    // Getters and Setters
    public int getSettingId() { return settingId; }
    public void setSettingId(int settingId) { this.settingId = settingId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { 
        this.settingValue = settingValue; 
    }

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
