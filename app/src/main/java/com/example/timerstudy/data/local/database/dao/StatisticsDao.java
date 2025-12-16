package com.example.timerstudy.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.timerstudy.data.local.database.entities.StatisticsCacheEntity;
import com.example.timerstudy.data.local.database.entities.StreakRecordEntity;

import java.util.List;

/**
 * Data Access Object for Statistics operations
 * Provides methods to interact with the statistics_cache and streak_records tables
 */
@Dao
public interface StatisticsDao {
    
    // ==================== STATISTICS CACHE OPERATIONS ====================
    
    // CREATE OPERATIONS
    /**
     * Insert a new statistics cache entry
     * @param cache StatisticsCacheEntity to insert
     * @return The ID of the inserted cache entry
     */
    @Insert
    long insertStatisticsCache(StatisticsCacheEntity cache);
    
    /**
     * Insert multiple statistics cache entries
     * @param caches List of StatisticsCacheEntity to insert
     * @return List of inserted cache IDs
     */
    @Insert
    List<Long> insertStatisticsCaches(List<StatisticsCacheEntity> caches);
    
    // READ OPERATIONS
    /**
     * Get all statistics cache entries
     * @return List of all statistics cache entries
     */
    @Query("SELECT * FROM statistics_cache ORDER BY cached_at DESC")
    List<StatisticsCacheEntity> getAllStatisticsCache();
    
    /**
     * Get statistics cache by ID
     * @param cacheId Cache ID to search for
     * @return StatisticsCacheEntity or null if not found
     */
    @Query("SELECT * FROM statistics_cache WHERE cache_id = :cacheId")
    StatisticsCacheEntity getStatisticsCacheById(int cacheId);
    
    /**
     * Get statistics cache by user ID
     * @param userId User ID to filter by
     * @return List of cache entries for the user
     */
    @Query("SELECT * FROM statistics_cache WHERE user_id = :userId ORDER BY cached_at DESC")
    List<StatisticsCacheEntity> getStatisticsCacheByUserId(long userId);
    
    /**
     * Get statistics cache by user ID and cache type
     * @param userId User ID to filter by
     * @param cacheType Cache type (DAILY, MONTHLY, YEARLY)
     * @return List of cache entries for the user and type
     */
    @Query("SELECT * FROM statistics_cache WHERE user_id = :userId AND cache_type = :cacheType ORDER BY cached_at DESC")
    List<StatisticsCacheEntity> getStatisticsCacheByUserAndType(long userId, String cacheType);
    
    /**
     * Get statistics cache by user ID, cache type, and date
     * @param userId User ID to filter by
     * @param cacheType Cache type (DAILY, MONTHLY, YEARLY)
     * @param cacheDate Cache date timestamp
     * @return StatisticsCacheEntity or null if not found
     */
    @Query("SELECT * FROM statistics_cache WHERE user_id = :userId AND cache_type = :cacheType AND cache_date = :cacheDate")
    StatisticsCacheEntity getStatisticsCacheByUserTypeAndDate(long userId, String cacheType, long cacheDate);
    
    /**
     * Get daily statistics cache by user ID
     * @param userId User ID to filter by
     * @return List of daily cache entries for the user
     */
    @Query("SELECT * FROM statistics_cache WHERE user_id = :userId AND cache_type = 'DAILY' ORDER BY cache_date DESC")
    List<StatisticsCacheEntity> getDailyStatisticsCacheByUser(long userId);
    
    /**
     * Get monthly statistics cache by user ID
     * @param userId User ID to filter by
     * @return List of monthly cache entries for the user
     */
    @Query("SELECT * FROM statistics_cache WHERE user_id = :userId AND cache_type = 'MONTHLY' ORDER BY cache_date DESC")
    List<StatisticsCacheEntity> getMonthlyStatisticsCacheByUser(long userId);
    
    /**
     * Get yearly statistics cache by user ID
     * @param userId User ID to filter by
     * @return List of yearly cache entries for the user
     */
    @Query("SELECT * FROM statistics_cache WHERE user_id = :userId AND cache_type = 'YEARLY' ORDER BY cache_date DESC")
    List<StatisticsCacheEntity> getYearlyStatisticsCacheByUser(long userId);
    
    /**
     * Get latest statistics cache by user ID and type
     * @param userId User ID to filter by
     * @param cacheType Cache type (DAILY, MONTHLY, YEARLY)
     * @return Latest cache entry for the user and type
     */
    @Query("SELECT * FROM statistics_cache WHERE user_id = :userId AND cache_type = :cacheType ORDER BY cached_at DESC LIMIT 1")
    StatisticsCacheEntity getLatestStatisticsCacheByUserAndType(long userId, String cacheType);
    
    /**
     * Get statistics cache by date range
     * @param userId User ID to filter by
     * @param startDate Start date timestamp
     * @param endDate End date timestamp
     * @return List of cache entries in date range
     */
    @Query("SELECT * FROM statistics_cache WHERE user_id = :userId AND cache_date BETWEEN :startDate AND :endDate ORDER BY cache_date DESC")
    List<StatisticsCacheEntity> getStatisticsCacheByDateRange(long userId, long startDate, long endDate);
    
    /**
     * Get cache count by user
     * @param userId User ID to filter by
     * @return Number of cache entries for the user
     */
    @Query("SELECT COUNT(*) FROM statistics_cache WHERE user_id = :userId")
    int getCacheCountByUser(long userId);
    
    // UPDATE OPERATIONS
    /**
     * Update statistics cache information
     * @param cache StatisticsCacheEntity with updated information
     */
    @Update
    void updateStatisticsCache(StatisticsCacheEntity cache);
    
    /**
     * Update statistics cache data
     * @param cacheId Cache ID to update
     * @param totalSessions New total sessions count
     * @param totalFocusTime New total focus time
     * @param totalBreakTime New total break time
     * @param completedTasks New completed tasks count
     * @param goalAchieved New goal achieved status
     * @param currentStreak New current streak
     * @param bestStreak New best streak
     */
    @Query("UPDATE statistics_cache SET total_sessions = :totalSessions, total_focus_time = :totalFocusTime, " +
           "total_break_time = :totalBreakTime, completed_tasks = :completedTasks, goal_achieved = :goalAchieved, " +
           "current_streak = :currentStreak, best_streak = :bestStreak, cached_at = :cachedAt WHERE cache_id = :cacheId")
    void updateStatisticsCacheData(int cacheId, int totalSessions, int totalFocusTime, int totalBreakTime, 
                                  int completedTasks, boolean goalAchieved, int currentStreak, int bestStreak, long cachedAt);
    
    // DELETE OPERATIONS
    /**
     * Delete statistics cache by ID
     * @param cache StatisticsCacheEntity to delete
     */
    @Delete
    void deleteStatisticsCache(StatisticsCacheEntity cache);
    
    /**
     * Delete statistics cache by ID
     * @param cacheId Cache ID to delete
     */
    @Query("DELETE FROM statistics_cache WHERE cache_id = :cacheId")
    void deleteStatisticsCacheById(int cacheId);
    
    /**
     * Delete statistics cache by user ID
     * @param userId User ID to filter by
     */
    @Query("DELETE FROM statistics_cache WHERE user_id = :userId")
    void deleteStatisticsCacheByUser(long userId);
    
    /**
     * Delete old statistics cache entries
     * @param timestamp Timestamp to compare
     */
    @Query("DELETE FROM statistics_cache WHERE cached_at < :timestamp")
    void deleteOldStatisticsCache(long timestamp);
    
    /**
     * Delete all statistics cache (use with caution)
     */
    @Query("DELETE FROM statistics_cache")
    void deleteAllStatisticsCache();
    
    // ==================== STREAK RECORDS OPERATIONS ====================
    
    // CREATE OPERATIONS
    /**
     * Insert a new streak record
     * @param streak StreakRecordEntity to insert
     * @return The ID of the inserted streak record
     */
    @Insert
    long insertStreakRecord(StreakRecordEntity streak);
    
    /**
     * Insert multiple streak records
     * @param streaks List of StreakRecordEntity to insert
     * @return List of inserted streak record IDs
     */
    @Insert
    List<Long> insertStreakRecords(List<StreakRecordEntity> streaks);
    
    // READ OPERATIONS
    /**
     * Get all streak records
     * @return List of all streak records
     */
    @Query("SELECT * FROM streak_records ORDER BY streak_date DESC")
    List<StreakRecordEntity> getAllStreakRecords();
    
    /**
     * Get streak record by ID
     * @param streakId Streak ID to search for
     * @return StreakRecordEntity or null if not found
     */
    @Query("SELECT * FROM streak_records WHERE streak_id = :streakId")
    StreakRecordEntity getStreakRecordById(int streakId);
    
    /**
     * Get streak records by user ID
     * @param userId User ID to filter by
     * @return List of streak records for the user
     */
    @Query("SELECT * FROM streak_records WHERE user_id = :userId ORDER BY streak_date DESC")
    List<StreakRecordEntity> getStreakRecordsByUserId(long userId);
    
    /**
     * Get streak record by user ID and date
     * @param userId User ID to filter by
     * @param streakDate Streak date timestamp
     * @return StreakRecordEntity or null if not found
     */
    @Query("SELECT * FROM streak_records WHERE user_id = :userId AND streak_date = :streakDate")
    StreakRecordEntity getStreakRecordByUserAndDate(long userId, long streakDate);
    
    /**
     * Get streak records by user ID and date range
     * @param userId User ID to filter by
     * @param startDate Start date timestamp
     * @param endDate End date timestamp
     * @return List of streak records in date range
     */
    @Query("SELECT * FROM streak_records WHERE user_id = :userId AND streak_date BETWEEN :startDate AND :endDate ORDER BY streak_date DESC")
    List<StreakRecordEntity> getStreakRecordsByUserAndDateRange(long userId, long startDate, long endDate);
    
    /**
     * Get active streak days by user ID
     * @param userId User ID to filter by
     * @return List of days with activity
     */
    @Query("SELECT * FROM streak_records WHERE user_id = :userId AND has_activity = 1 ORDER BY streak_date DESC")
    List<StreakRecordEntity> getActiveStreakDaysByUser(long userId);
    
    /**
     * Get inactive streak days by user ID
     * @param userId User ID to filter by
     * @return List of days without activity
     */
    @Query("SELECT * FROM streak_records WHERE user_id = :userId AND has_activity = 0 ORDER BY streak_date DESC")
    List<StreakRecordEntity> getInactiveStreakDaysByUser(long userId);
    
    /**
     * Get current streak by user ID
     * @param userId User ID to filter by
     * @param currentDate Current date timestamp
     * @return Current streak count
     */
    @Query("SELECT COUNT(*) FROM streak_records WHERE user_id = :userId AND has_activity = 1 AND streak_date <= :currentDate ORDER BY streak_date DESC")
    int getCurrentStreakByUser(long userId, long currentDate);
    
    /**
     * Get longest streak by user ID
     * @param userId User ID to filter by
     * @return Longest streak count
     */
    @Query("SELECT MAX(session_count) FROM streak_records WHERE user_id = :userId AND has_activity = 1")
    int getLongestStreakByUser(long userId);
    
    /**
     * Get total focus time by user ID
     * @param userId User ID to filter by
     * @return Total focus time in minutes
     */
    @Query("SELECT COALESCE(SUM(focus_time), 0) FROM streak_records WHERE user_id = :userId")
    int getTotalFocusTimeByUser(long userId);
    
    /**
     * Get total focus time by user ID and date range
     * @param userId User ID to filter by
     * @param startDate Start date timestamp
     * @param endDate End date timestamp
     * @return Total focus time in minutes for the date range
     */
    @Query("SELECT COALESCE(SUM(focus_time), 0) FROM streak_records WHERE user_id = :userId AND streak_date BETWEEN :startDate AND :endDate")
    int getTotalFocusTimeByUserAndDateRange(long userId, long startDate, long endDate);
    
    /**
     * Get total sessions by user ID
     * @param userId User ID to filter by
     * @return Total session count
     */
    @Query("SELECT COALESCE(SUM(session_count), 0) FROM streak_records WHERE user_id = :userId")
    int getTotalSessionsByUser(long userId);
    
    /**
     * Get total sessions by user ID and date range
     * @param userId User ID to filter by
     * @param startDate Start date timestamp
     * @param endDate End date timestamp
     * @return Total session count for the date range
     */
    @Query("SELECT COALESCE(SUM(session_count), 0) FROM streak_records WHERE user_id = :userId AND streak_date BETWEEN :startDate AND :endDate")
    int getTotalSessionsByUserAndDateRange(long userId, long startDate, long endDate);
    
    /**
     * Get streak record count by user
     * @param userId User ID to filter by
     * @return Number of streak records for the user
     */
    @Query("SELECT COUNT(*) FROM streak_records WHERE user_id = :userId")
    int getStreakRecordCountByUser(long userId);
    
    /**
     * Get active streak days count by user
     * @param userId User ID to filter by
     * @return Number of active streak days for the user
     */
    @Query("SELECT COUNT(*) FROM streak_records WHERE user_id = :userId AND has_activity = 1")
    int getActiveStreakDaysCountByUser(long userId);
    
    // UPDATE OPERATIONS
    /**
     * Update streak record information
     * @param streak StreakRecordEntity with updated information
     */
    @Update
    void updateStreakRecord(StreakRecordEntity streak);
    
    /**
     * Update streak record activity
     * @param userId User ID to filter by
     * @param streakDate Streak date timestamp
     * @param hasActivity New activity status
     * @param sessionCount New session count
     * @param focusTime New focus time
     */
    @Query("UPDATE streak_records SET has_activity = :hasActivity, session_count = :sessionCount, focus_time = :focusTime WHERE user_id = :userId AND streak_date = :streakDate")
    void updateStreakRecordActivity(long userId, long streakDate, boolean hasActivity, int sessionCount, int focusTime);
    
    /**
     * Mark day as active
     * @param userId User ID to filter by
     * @param streakDate Streak date timestamp
     * @param sessionCount Session count for the day
     * @param focusTime Focus time for the day
     */
    @Query("UPDATE streak_records SET has_activity = 1, session_count = :sessionCount, focus_time = :focusTime WHERE user_id = :userId AND streak_date = :streakDate")
    void markDayAsActive(long userId, long streakDate, int sessionCount, int focusTime);
    
    /**
     * Mark day as inactive
     * @param userId User ID to filter by
     * @param streakDate Streak date timestamp
     */
    @Query("UPDATE streak_records SET has_activity = 0, session_count = 0, focus_time = 0 WHERE user_id = :userId AND streak_date = :streakDate")
    void markDayAsInactive(long userId, long streakDate);
    
    // DELETE OPERATIONS
    /**
     * Delete streak record by ID
     * @param streak StreakRecordEntity to delete
     */
    @Delete
    void deleteStreakRecord(StreakRecordEntity streak);
    
    /**
     * Delete streak record by ID
     * @param streakId Streak ID to delete
     */
    @Query("DELETE FROM streak_records WHERE streak_id = :streakId")
    void deleteStreakRecordById(int streakId);
    
    /**
     * Delete streak records by user ID
     * @param userId User ID to filter by
     */
    @Query("DELETE FROM streak_records WHERE user_id = :userId")
    void deleteStreakRecordsByUser(long userId);
    
    /**
     * Delete old streak records
     * @param timestamp Timestamp to compare
     */
    @Query("DELETE FROM streak_records WHERE streak_date < :timestamp")
    void deleteOldStreakRecords(long timestamp);
    
    /**
     * Delete all streak records (use with caution)
     */
    @Query("DELETE FROM streak_records")
    void deleteAllStreakRecords();
}
