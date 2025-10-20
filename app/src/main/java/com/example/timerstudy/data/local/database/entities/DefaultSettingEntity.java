package com.example.timerstudy.data.local.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "default_settings",
    indices = {
        @Index(value = "setting_key", unique = true),
        @Index(value = "category")
    }
)
public class DefaultSettingEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "default_setting_id")
    private int defaultSettingId;

    @ColumnInfo(name = "setting_key")
    private String settingKey;

    @ColumnInfo(name = "default_value")
    private String defaultValue;

    @ColumnInfo(name = "data_type")
    private String dataType;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "is_configurable", defaultValue = "1")
    private boolean isConfigurable;

    // Constants
    public static final String CATEGORY_TIMER = "TIMER";
    public static final String CATEGORY_NOTIFICATION = "NOTIFICATION";
    public static final String CATEGORY_APPEARANCE = "APPEARANCE";
    public static final String CATEGORY_DATA = "DATA";

    // Getters and Setters
    public int getDefaultSettingId() { return defaultSettingId; }
    public void setDefaultSettingId(int defaultSettingId) { 
        this.defaultSettingId = defaultSettingId; 
    }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { 
        this.defaultValue = defaultValue; 
    }

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isConfigurable() { return isConfigurable; }
    public void setConfigurable(boolean configurable) { isConfigurable = configurable; }
}
