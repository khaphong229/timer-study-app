package com.example.timerstudy.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.timerstudy.data.local.database.entities.UserEntity;

import java.util.List;

/**
 * Data Access Object for User operations
 * Provides methods to interact with the users table
 */
@Dao
public interface UserDao {
    
    // ==================== CREATE OPERATIONS ====================
    
    /**
     * Insert a new user
     * @param user UserEntity to insert
     * @return The ID of the inserted user
     */
    @Insert
    long insertUser(UserEntity user);
    
    /**
     * Insert multiple users
     * @param users List of UserEntity to insert
     * @return List of inserted user IDs
     */
    @Insert
    List<Long> insertUsers(List<UserEntity> users);
    
    // ==================== READ OPERATIONS ====================
    
    /**
     * Get all users
     * @return List of all users
     */
    @Query("SELECT * FROM users ORDER BY created_at DESC")
    List<UserEntity> getAllUsers();
    
    /**
     * Get user by ID
     * @param userId User ID to search for
     * @return UserEntity or null if not found
     */
    @Query("SELECT * FROM users WHERE user_id = :userId")
    UserEntity getUserById(int userId);
    
    /**
     * Get user by email
     * @param email Email to search for
     * @return UserEntity or null if not found
     */
    @Query("SELECT * FROM users WHERE email = :email")
    UserEntity getUserByEmail(String email);
    
    /**
     * Get anonymous users
     * @return List of anonymous users
     */
    @Query("SELECT * FROM users WHERE is_anonymous = 1")
    List<UserEntity> getAnonymousUsers();
    
    /**
     * Get non-anonymous users
     * @return List of registered users
     */
    @Query("SELECT * FROM users WHERE is_anonymous = 0")
    List<UserEntity> getRegisteredUsers();
    
    /**
     * Get users created after a specific date
     * @param timestamp Timestamp to compare
     * @return List of users created after timestamp
     */
    @Query("SELECT * FROM users WHERE created_at > :timestamp")
    List<UserEntity> getUsersCreatedAfter(long timestamp);
    
    /**
     * Get users with recent login
     * @param timestamp Timestamp to compare
     * @return List of users with recent login
     */
    @Query("SELECT * FROM users WHERE last_login > :timestamp")
    List<UserEntity> getUsersWithRecentLogin(long timestamp);
    
    /**
     * Check if email exists
     * @param email Email to check
     * @return True if email exists, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    boolean isEmailExists(String email);
    
    /**
     * Get user count
     * @return Total number of users
     */
    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();
    
    /**
     * Get anonymous user count
     * @return Number of anonymous users
     */
    @Query("SELECT COUNT(*) FROM users WHERE is_anonymous = 1")
    int getAnonymousUserCount();
    
    // ==================== UPDATE OPERATIONS ====================
    
    /**
     * Update user information
     * @param user UserEntity with updated information
     */
    @Update
    void updateUser(UserEntity user);
    
    /**
     * Update user's last login time
     * @param userId User ID to update
     * @param lastLogin New last login timestamp
     */
    @Query("UPDATE users SET last_login = :lastLogin WHERE user_id = :userId")
    void updateLastLogin(int userId, long lastLogin);
    
    /**
     * Update user's display name
     * @param userId User ID to update
     * @param displayName New display name
     */
    @Query("UPDATE users SET display_name = :displayName WHERE user_id = :userId")
    void updateDisplayName(int userId, String displayName);
    
    /**
     * Update user's profile picture URL
     * @param userId User ID to update
     * @param profilePictureUrl New profile picture URL
     */
    @Query("UPDATE users SET profile_picture_url = :profilePictureUrl WHERE user_id = :userId")
    void updateProfilePicture(int userId, String profilePictureUrl);
    
    /**
     * Convert anonymous user to registered user
     * @param userId User ID to convert
     * @param email User's email
     * @param displayName User's display name
     */
    @Query("UPDATE users SET is_anonymous = 0, email = :email, display_name = :displayName WHERE user_id = :userId")
    void convertToRegisteredUser(int userId, String email, String displayName);
    
    // ==================== DELETE OPERATIONS ====================
    
    /**
     * Delete user by ID
     * @param userId User ID to delete
     */
    @Delete
    void deleteUser(UserEntity user);
    
    /**
     * Delete user by ID
     * @param userId User ID to delete
     */
    @Query("DELETE FROM users WHERE user_id = :userId")
    void deleteUserById(int userId);
    
    /**
     * Delete all anonymous users
     */
    @Query("DELETE FROM users WHERE is_anonymous = 1")
    void deleteAllAnonymousUsers();
    
    /**
     * Delete users created before a specific date
     * @param timestamp Timestamp to compare
     */
    @Query("DELETE FROM users WHERE created_at < :timestamp")
    void deleteUsersCreatedBefore(long timestamp);
    
    /**
     * Delete all users (use with caution)
     */
    @Query("DELETE FROM users")
    void deleteAllUsers();
}
