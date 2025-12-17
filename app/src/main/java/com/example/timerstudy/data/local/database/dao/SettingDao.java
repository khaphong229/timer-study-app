package com.example.timerstudy.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.timerstudy.data.local.database.entities.DefaultSettingEntity;
import com.example.timerstudy.data.local.database.entities.UserSettingEntity;

import java.util.List;

/**
 * Data Access Object for Settings operations
 * Provides methods to interact with the user_settings and default_settings tables
 */
@Dao
public interface SettingDao {
    
    // ==================== USER SETTINGS OPERATIONS ====================
    
    // CREATE OPERATIONS
    /**
     * Insert a new user setting
     * @param setting UserSettingEntity to insert
     * @return The ID of the inserted setting
     */
    @Insert
    long insertUserSetting(UserSettingEntity setting);
    
    /**
     * Insert multiple user settings
     * @param settings List of UserSettingEntity to insert
     * @return List of inserted setting IDs
     */
    @Insert
    List<Long> insertUserSettings(List<UserSettingEntity> settings);
    
    // READ OPERATIONS
    /**
     * Get all user settings
     * @return List of all user settings
     */
    @Query("SELECT * FROM user_settings ORDER BY created_at DESC")
    List<UserSettingEntity> getAllUserSettings();
    
    /**
     * Get user setting by ID
     * @param settingId Setting ID to search for
     * @return UserSettingEntity or null if not found
     */
    @Query("SELECT * FROM user_settings WHERE setting_id = :settingId")
    UserSettingEntity getUserSettingById(int settingId);
    
    /**
     * Get user settings by user ID
     * @param userId User ID to filter by
     * @return List of settings for the user
     */
    @Query("SELECT * FROM user_settings WHERE user_id = :userId ORDER BY created_at DESC")
    List<UserSettingEntity> getUserSettingsByUserId(long userId);
    
    /**
     * Get user setting by user ID and key
     * @param userId User ID to filter by
     * @param settingKey Setting key to search for
     * @return UserSettingEntity or null if not found
     */
    @Query("SELECT * FROM user_settings WHERE user_id = :userId AND setting_key = :settingKey")
    UserSettingEntity getUserSettingByKey(long userId, String settingKey);
    
    /**
     * Get user settings by data type
     * @param userId User ID to filter by
     * @param dataType Data type to filter by
     * @return List of settings with the specified data type
     */
    @Query("SELECT * FROM user_settings WHERE user_id = :userId AND data_type = :dataType ORDER BY created_at DESC")
    List<UserSettingEntity> getUserSettingsByDataType(long userId, String dataType);
    
    /**
     * Get user setting value by key
     * @param userId User ID to filter by
     * @param settingKey Setting key to search for
     * @return Setting value or null if not found
     */
    @Query("SELECT setting_value FROM user_settings WHERE user_id = :userId AND setting_key = :settingKey")
    String getUserSettingValue(long userId, String settingKey);
    
    /**
     * Check if user setting exists
     * @param userId User ID to filter by
     * @param settingKey Setting key to check
     * @return True if setting exists, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM user_settings WHERE user_id = :userId AND setting_key = :settingKey)")
    boolean userSettingExists(long userId, String settingKey);
    
    /**
     * Get user setting count
     * @param userId User ID to filter by
     * @return Number of settings for the user
     */
    @Query("SELECT COUNT(*) FROM user_settings WHERE user_id = :userId")
    int getUserSettingCount(long userId);
    
    // UPDATE OPERATIONS
    /**
     * Update user setting information
     * @param setting UserSettingEntity with updated information
     */
    @Update
    void updateUserSetting(UserSettingEntity setting);
    
    /**
     * Update user setting value
     * @param userId User ID to filter by
     * @param settingKey Setting key to update
     * @param settingValue New setting value
     */
    @Query("UPDATE user_settings SET setting_value = :settingValue, updated_at = :updatedAt WHERE user_id = :userId AND setting_key = :settingKey")
    void updateUserSettingValue(long userId, String settingKey, String settingValue, long updatedAt);
    
    /**
     * Update user setting value and data type
     * @param userId User ID to filter by
     * @param settingKey Setting key to update
     * @param settingValue New setting value
     * @param dataType New data type
     */
    @Query("UPDATE user_settings SET setting_value = :settingValue, data_type = :dataType, updated_at = :updatedAt WHERE user_id = :userId AND setting_key = :settingKey")
    void updateUserSettingValueAndType(long userId, String settingKey, String settingValue, String dataType, long updatedAt);
    
    // DELETE OPERATIONS
    /**
     * Delete user setting by ID
     * @param setting UserSettingEntity to delete
     */
    @Delete
    void deleteUserSetting(UserSettingEntity setting);
    
    /**
     * Delete user setting by ID
     * @param settingId Setting ID to delete
     */
    @Query("DELETE FROM user_settings WHERE setting_id = :settingId")
    void deleteUserSettingById(int settingId);
    
    /**
     * Delete user setting by key
     * @param userId User ID to filter by
     * @param settingKey Setting key to delete
     */
    @Query("DELETE FROM user_settings WHERE user_id = :userId AND setting_key = :settingKey")
    void deleteUserSettingByKey(long userId, String settingKey);
    
    /**
     * Delete all user settings by user ID
     * @param userId User ID to filter by
     */
    @Query("DELETE FROM user_settings WHERE user_id = :userId")
    void deleteAllUserSettingsByUser(long userId);
    
    /**
     * Delete all user settings (use with caution)
     */
    @Query("DELETE FROM user_settings")
    void deleteAllUserSettings();
    
    // ==================== DEFAULT SETTINGS OPERATIONS ====================
    
    // CREATE OPERATIONS
    /**
     * Insert a new default setting
     * @param setting DefaultSettingEntity to insert
     * @return The ID of the inserted setting
     */
    @Insert
    long insertDefaultSetting(DefaultSettingEntity setting);
    
    /**
     * Insert multiple default settings
     * @param settings List of DefaultSettingEntity to insert
     * @return List of inserted setting IDs
     */
    @Insert
    List<Long> insertDefaultSettings(List<DefaultSettingEntity> settings);
    
    // READ OPERATIONS
    /**
     * Get all default settings
     * @return List of all default settings
     */
    @Query("SELECT * FROM default_settings ORDER BY category ASC, setting_key ASC")
    List<DefaultSettingEntity> getAllDefaultSettings();
    
    /**
     * Get default setting by ID
     * @param settingId Setting ID to search for
     * @return DefaultSettingEntity or null if not found
     */
    @Query("SELECT * FROM default_settings WHERE default_setting_id = :settingId")
    DefaultSettingEntity getDefaultSettingById(int settingId);
    
    /**
     * Get default setting by key
     * @param settingKey Setting key to search for
     * @return DefaultSettingEntity or null if not found
     */
    @Query("SELECT * FROM default_settings WHERE setting_key = :settingKey")
    DefaultSettingEntity getDefaultSettingByKey(String settingKey);
    
    /**
     * Get default settings by category
     * @param category Category to filter by
     * @return List of settings in the specified category
     */
    @Query("SELECT * FROM default_settings WHERE category = :category ORDER BY setting_key ASC")
    List<DefaultSettingEntity> getDefaultSettingsByCategory(String category);
    
    /**
     * Get configurable default settings
     * @return List of configurable default settings
     */
    @Query("SELECT * FROM default_settings WHERE is_configurable = 1 ORDER BY category ASC, setting_key ASC")
    List<DefaultSettingEntity> getConfigurableDefaultSettings();
    
    /**
     * Get non-configurable default settings
     * @return List of non-configurable default settings
     */
    @Query("SELECT * FROM default_settings WHERE is_configurable = 0 ORDER BY category ASC, setting_key ASC")
    List<DefaultSettingEntity> getNonConfigurableDefaultSettings();
    
    /**
     * Get default settings by data type
     * @param dataType Data type to filter by
     * @return List of settings with the specified data type
     */
    @Query("SELECT * FROM default_settings WHERE data_type = :dataType ORDER BY category ASC, setting_key ASC")
    List<DefaultSettingEntity> getDefaultSettingsByDataType(String dataType);
    
    /**
     * Get default setting value by key
     * @param settingKey Setting key to search for
     * @return Default setting value or null if not found
     */
    @Query("SELECT default_value FROM default_settings WHERE setting_key = :settingKey")
    String getDefaultSettingValue(String settingKey);
    
    /**
     * Check if default setting exists
     * @param settingKey Setting key to check
     * @return True if setting exists, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM default_settings WHERE setting_key = :settingKey)")
    boolean defaultSettingExists(String settingKey);
    
    /**
     * Get default setting count
     * @return Number of default settings
     */
    @Query("SELECT COUNT(*) FROM default_settings")
    int getDefaultSettingCount();
    
    /**
     * Get default setting count by category
     * @param category Category to filter by
     * @return Number of settings in the category
     */
    @Query("SELECT COUNT(*) FROM default_settings WHERE category = :category")
    int getDefaultSettingCountByCategory(String category);
    
    // UPDATE OPERATIONS
    /**
     * Update default setting information
     * @param setting DefaultSettingEntity with updated information
     */
    @Update
    void updateDefaultSetting(DefaultSettingEntity setting);
    
    /**
     * Update default setting value
     * @param settingKey Setting key to update
     * @param defaultValue New default value
     */
    @Query("UPDATE default_settings SET default_value = :defaultValue WHERE setting_key = :settingKey")
    void updateDefaultSettingValue(String settingKey, String defaultValue);
    
    /**
     * Update default setting configurable status
     * @param settingKey Setting key to update
     * @param isConfigurable New configurable status
     */
    @Query("UPDATE default_settings SET is_configurable = :isConfigurable WHERE setting_key = :settingKey")
    void updateDefaultSettingConfigurable(String settingKey, boolean isConfigurable);
    
    // DELETE OPERATIONS
    /**
     * Delete default setting by ID
     * @param setting DefaultSettingEntity to delete
     */
    @Delete
    void deleteDefaultSetting(DefaultSettingEntity setting);
    
    /**
     * Delete default setting by ID
     * @param settingId Setting ID to delete
     */
    @Query("DELETE FROM default_settings WHERE default_setting_id = :settingId")
    void deleteDefaultSettingById(int settingId);
    
    /**
     * Delete default setting by key
     * @param settingKey Setting key to delete
     */
    @Query("DELETE FROM default_settings WHERE setting_key = :settingKey")
    void deleteDefaultSettingByKey(String settingKey);
    
    /**
     * Delete default settings by category
     * @param category Category to filter by
     */
    @Query("DELETE FROM default_settings WHERE category = :category")
    void deleteDefaultSettingsByCategory(String category);
    
    /**
     * Delete all default settings (use with caution)
     */
    @Query("DELETE FROM default_settings")
    void deleteAllDefaultSettings();
    
    // ==================== COMBINED OPERATIONS ====================
    
    /**
     * Get user setting or default setting value
     * @param userId User ID to filter by
     * @param settingKey Setting key to search for
     * @return User setting value if exists, otherwise default setting value
     */
    @Query("SELECT COALESCE(us.setting_value, ds.default_value) FROM user_settings us " +
           "LEFT JOIN default_settings ds ON us.setting_key = ds.setting_key " +
           "WHERE us.user_id = :userId AND us.setting_key = :settingKey " +
           "UNION " +
           "SELECT ds.default_value FROM default_settings ds " +
           "WHERE ds.setting_key = :settingKey " +
           "AND NOT EXISTS (SELECT 1 FROM user_settings us WHERE us.user_id = :userId AND us.setting_key = :settingKey)")
    String getUserOrDefaultSettingValue(long userId, String settingKey);
    
    /**
     * Get all settings for user (user settings + default settings)
     * @param userId User ID to filter by
     * @return List of all settings available to the user
     */
    @Query("SELECT us.* FROM user_settings us WHERE us.user_id = :userId " +
            "UNION " +
            "SELECT ds.default_setting_id as setting_id, " +
            ":userId as user_id, " +
            "ds.setting_key, " +
            "ds.default_value as setting_value, " +
            "ds.data_type, " +
            "NULL as created_at, " +
            "NULL as updated_at " +
            "FROM default_settings ds " +
            "WHERE NOT EXISTS (" +
            "SELECT 1 FROM user_settings us WHERE us.user_id = :userId AND us.setting_key = ds.setting_key" +
            ")")
    List<UserSettingEntity> getAllSettingsForUser(long userId);

}
