package com.example.timerstudy.data.local.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.timerstudy.data.local.database.converters.DateConverter;

import java.util.Date;

@Entity(
    tableName = "users",
    indices = {
        @Index(value = "email"),
        @Index(value = "created_at")
    }
)
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "display_name")
    private String displayName;

    @ColumnInfo(name = "profile_picture_url")
    private String profilePictureUrl;

    @ColumnInfo(name = "created_at")
    @TypeConverters(DateConverter.class)
    private Date createdAt;

    @ColumnInfo(name = "last_login")
    @TypeConverters(DateConverter.class)
    private Date lastLogin;

    @ColumnInfo(name = "is_anonymous", defaultValue = "1")
    private boolean isAnonymous;

    @ColumnInfo(name = "total_coins", defaultValue = "0")
    private int totalCoins;

    // Constructor
    public UserEntity() {
        this.createdAt = new Date();
        this.isAnonymous = true;
    }

    // Getters and Setters
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { 
        this.profilePictureUrl = profilePictureUrl; 
    }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getLastLogin() { return lastLogin; }
    public void setLastLogin(Date lastLogin) { this.lastLogin = lastLogin; }

    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }

    public int getTotalCoins() { return totalCoins; }
    public void setTotalCoins(int totalCoins) { this.totalCoins = totalCoins; }
}
