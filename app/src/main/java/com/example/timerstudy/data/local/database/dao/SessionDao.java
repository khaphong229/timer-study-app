package com.example.timerstudy.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.timerstudy.data.local.database.entities.SessionEntity;

import java.util.List;

/**
 * Data Access Object for Session operations
 * Provides methods to interact with the sessions table
 */
@Dao
public interface SessionDao {
    
    // ==================== CREATE OPERATIONS ====================
    
    /**
     * Insert a new session
     * @param session SessionEntity to insert
     * @return The ID of the inserted session
     */
    @Insert
    long insertSession(SessionEntity session);
    
    /**
     * Insert multiple sessions
     * @param sessions List of SessionEntity to insert
     * @return List of inserted session IDs
     */
    @Insert
    List<Long> insertSessions(List<SessionEntity> sessions);
    
    // ==================== READ OPERATIONS ====================
    
    /**
     * Get all sessions
     * @return List of all sessions
     */
    @Query("SELECT * FROM sessions ORDER BY created_at DESC")
    List<SessionEntity> getAllSessions();
    
    /**
     * Get session by ID
     * @param sessionId Session ID to search for
     * @return SessionEntity or null if not found
     */
    @Query("SELECT * FROM sessions WHERE session_id = :sessionId")
    SessionEntity getSessionById(int sessionId);
    
    /**
     * Get sessions by user ID
     * @param userId User ID to filter by
     * @return List of sessions for the user
     */
    @Query("SELECT * FROM sessions WHERE user_id = :userId ORDER BY session_date DESC")
    List<SessionEntity> getSessionsByUserId(int userId);
    
    /**
     * Get sessions by user ID and date range
     * @param userId User ID to filter by
     * @param startDate Start date timestamp
     * @param endDate End date timestamp
     * @return List of sessions in date range
     */
    @Query("SELECT * FROM sessions WHERE user_id = :userId AND session_date BETWEEN :startDate AND :endDate ORDER BY session_date DESC")
    List<SessionEntity> getSessionsByUserAndDateRange(int userId, long startDate, long endDate);
    
    /**
     * Get sessions by type
     * @param sessionType Type of session (FOCUS_SESSION, SHORT_BREAK, LONG_BREAK)
     * @return List of sessions of the specified type
     */
    @Query("SELECT * FROM sessions WHERE session_type = :sessionType ORDER BY created_at DESC")
    List<SessionEntity> getSessionsByType(String sessionType);
    
    /**
     * Get sessions by status
     * @param status Status of session (IN_PROGRESS, COMPLETED, PAUSED, CANCELLED)
     * @return List of sessions with the specified status
     */
    @Query("SELECT * FROM sessions WHERE status = :status ORDER BY created_at DESC")
    List<SessionEntity> getSessionsByStatus(String status);
    
    /**
     * Get completed sessions
     * @return List of completed sessions
     */
    @Query("SELECT * FROM sessions WHERE is_completed = 1 ORDER BY end_time DESC")
    List<SessionEntity> getCompletedSessions();
    
    /**
     * Get completed sessions by user
     * @param userId User ID to filter by
     * @return List of completed sessions for the user
     */
    @Query("SELECT * FROM sessions WHERE user_id = :userId AND is_completed = 1 ORDER BY end_time DESC")
    List<SessionEntity> getCompletedSessionsByUser(int userId);
    
    /**
     * Get sessions in progress
     * @return List of sessions currently in progress
     */
    @Query("SELECT * FROM sessions WHERE status = 'IN_PROGRESS' ORDER BY start_time DESC")
    List<SessionEntity> getSessionsInProgress();
    
    /**
     * Get sessions in progress by user
     * @param userId User ID to filter by
     * @return List of sessions currently in progress for the user
     */
    @Query("SELECT * FROM sessions WHERE user_id = :userId AND status = 'IN_PROGRESS' ORDER BY start_time DESC")
    List<SessionEntity> getSessionsInProgressByUser(int userId);
    
    /**
     * Get sessions for today
     * @param userId User ID to filter by
     * @param todayStart Start of today (timestamp)
     * @param todayEnd End of today (timestamp)
     * @return List of sessions for today
     */
    @Query("SELECT * FROM sessions WHERE user_id = :userId AND session_date BETWEEN :todayStart AND :todayEnd ORDER BY start_time ASC")
    List<SessionEntity> getTodaySessions(int userId, long todayStart, long todayEnd);
    
    /**
     * Get focus sessions by user
     * @param userId User ID to filter by
     * @return List of focus sessions for the user
     */
    @Query("SELECT * FROM sessions WHERE user_id = :userId AND session_type = 'FOCUS_SESSION' ORDER BY session_date DESC")
    List<SessionEntity> getFocusSessionsByUser(int userId);
    
    /**
     * Get break sessions by user
     * @param userId User ID to filter by
     * @return List of break sessions for the user
     */
    @Query("SELECT * FROM sessions WHERE user_id = :userId AND session_type IN ('SHORT_BREAK', 'LONG_BREAK') ORDER BY session_date DESC")
    List<SessionEntity> getBreakSessionsByUser(int userId);
    
    /**
     * Get session count by user
     * @param userId User ID to filter by
     * @return Number of sessions for the user
     */
    @Query("SELECT COUNT(*) FROM sessions WHERE user_id = :userId")
    int getSessionCountByUser(int userId);
    
    /**
     * Get completed session count by user
     * @param userId User ID to filter by
     * @return Number of completed sessions for the user
     */
    @Query("SELECT COUNT(*) FROM sessions WHERE user_id = :userId AND is_completed = 1")
    int getCompletedSessionCountByUser(int userId);
    
    /**
     * Get total focus time by user (in minutes)
     * @param userId User ID to filter by
     * @return Total focus time in minutes
     */
    @Query("SELECT COALESCE(SUM(actual_duration_minutes), 0) FROM sessions WHERE user_id = :userId AND session_type = 'FOCUS_SESSION' AND is_completed = 1")
    int getTotalFocusTimeByUser(int userId);
    
    /**
     * Get total focus time by user for a date range
     * @param userId User ID to filter by
     * @param startDate Start date timestamp
     * @param endDate End date timestamp
     * @return Total focus time in minutes for the date range
     */
    @Query("SELECT COALESCE(SUM(actual_duration_minutes), 0) FROM sessions WHERE user_id = :userId AND session_type = 'FOCUS_SESSION' AND is_completed = 1 AND session_date BETWEEN :startDate AND :endDate")
    int getTotalFocusTimeByUserAndDateRange(int userId, long startDate, long endDate);
    
    /**
     * Get average session duration by user
     * @param userId User ID to filter by
     * @return Average session duration in minutes
     */

    @Query("SELECT COUNT(*) FROM sessions " +
            "WHERE user_id = :userId " +
            "AND is_completed = 1 " +
            "AND session_date BETWEEN :startDate AND :endDate")
    int getCompletedSessionCountByUserAndDateRange(int userId, long startDate, long endDate);

    @Query("SELECT COALESCE(AVG(actual_duration_minutes), 0) FROM sessions WHERE user_id = :userId AND is_completed = 1 AND actual_duration_minutes IS NOT NULL")
    double getAverageSessionDurationByUser(int userId);
    
    /**
     * Get longest session by user
     * @param userId User ID to filter by
     * @return Longest session duration in minutes
     */
    @Query("SELECT COALESCE(MAX(actual_duration_minutes), 0) FROM sessions WHERE user_id = :userId AND is_completed = 1")
    int getLongestSessionByUser(int userId);
    
    // ==================== UPDATE OPERATIONS ====================
    
    /**
     * Update session information
     * @param session SessionEntity with updated information
     */
    @Update
    void updateSession(SessionEntity session);
    
    /**
     * Update session status
     * @param sessionId Session ID to update
     * @param status New status
     */
    @Query("UPDATE sessions SET status = :status, updated_at = :updatedAt WHERE session_id = :sessionId")
    void updateSessionStatus(int sessionId, String status, long updatedAt);
    
    /**
     * Complete a session
     * @param sessionId Session ID to complete
     * @param endTime End time timestamp
     * @param actualDuration Actual duration in minutes
     */
    @Query("UPDATE sessions SET status = 'COMPLETED', end_time = :endTime, actual_duration_minutes = :actualDuration, is_completed = 1, updated_at = :updatedAt WHERE session_id = :sessionId")
    void completeSession(int sessionId, long endTime, int actualDuration, long updatedAt);
    
    /**
     * Pause a session
     * @param sessionId Session ID to pause
     * @param pauseCount New pause count
     * @param totalPauseDuration Total pause duration in minutes
     */
    @Query("UPDATE sessions SET status = 'PAUSED', pause_count = :pauseCount, total_pause_duration = :totalPauseDuration, updated_at = :updatedAt WHERE session_id = :sessionId")
    void pauseSession(int sessionId, int pauseCount, int totalPauseDuration, long updatedAt);
    
    /**
     * Resume a session
     * @param sessionId Session ID to resume
     */
    @Query("UPDATE sessions SET status = 'IN_PROGRESS', updated_at = :updatedAt WHERE session_id = :sessionId")
    void resumeSession(int sessionId, long updatedAt);
    
    /**
     * Cancel a session
     * @param sessionId Session ID to cancel
     */
    @Query("UPDATE sessions SET status = 'CANCELLED', updated_at = :updatedAt WHERE session_id = :sessionId")
    void cancelSession(int sessionId, long updatedAt);
    
    /**
     * Update session pause count
     * @param sessionId Session ID to update
     * @param pauseCount New pause count
     * @param totalPauseDuration Total pause duration in minutes
     */
    @Query("UPDATE sessions SET pause_count = :pauseCount, total_pause_duration = :totalPauseDuration, updated_at = :updatedAt WHERE session_id = :sessionId")
    void updateSessionPauseInfo(int sessionId, int pauseCount, int totalPauseDuration, long updatedAt);
    
    // ==================== DELETE OPERATIONS ====================
    
    /**
     * Delete session by ID
     * @param session SessionEntity to delete
     */
    @Delete
    void deleteSession(SessionEntity session);
    
    /**
     * Delete session by ID
     * @param sessionId Session ID to delete
     */
    @Query("DELETE FROM sessions WHERE session_id = :sessionId")
    void deleteSessionById(int sessionId);
    
    /**
     * Delete sessions by user ID
     * @param userId User ID to filter by
     */
    @Query("DELETE FROM sessions WHERE user_id = :userId")
    void deleteSessionsByUser(int userId);
    
    /**
     * Delete cancelled sessions
     */
    @Query("DELETE FROM sessions WHERE status = 'CANCELLED'")
    void deleteCancelledSessions();
    
    /**
     * Delete sessions older than specified date
     * @param timestamp Timestamp to compare
     */
    @Query("DELETE FROM sessions WHERE created_at < :timestamp")
    void deleteSessionsOlderThan(long timestamp);
    
    /**
     * Delete all sessions (use with caution)
     */
    @Query("DELETE FROM sessions")
    void deleteAllSessions();
}
