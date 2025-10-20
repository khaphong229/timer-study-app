package com.example.timerstudy.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.timerstudy.data.local.database.entities.GoalEntity;

import java.util.List;

/**
 * Data Access Object for Goal operations
 * Provides methods to interact with the goals table
 */
@Dao
public interface GoalDao {
    
    // ==================== CREATE OPERATIONS ====================
    
    /**
     * Insert a new goal
     * @param goal GoalEntity to insert
     * @return The ID of the inserted goal
     */
    @Insert
    long insertGoal(GoalEntity goal);
    
    /**
     * Insert multiple goals
     * @param goals List of GoalEntity to insert
     * @return List of inserted goal IDs
     */
    @Insert
    List<Long> insertGoals(List<GoalEntity> goals);
    
    // ==================== READ OPERATIONS ====================
    
    /**
     * Get all goals
     * @return List of all goals
     */
    @Query("SELECT * FROM goals ORDER BY goal_date DESC")
    List<GoalEntity> getAllGoals();
    
    /**
     * Get goal by ID
     * @param goalId Goal ID to search for
     * @return GoalEntity or null if not found
     */
    @Query("SELECT * FROM goals WHERE goal_id = :goalId")
    GoalEntity getGoalById(int goalId);
    
    /**
     * Get goals by user ID
     * @param userId User ID to filter by
     * @return List of goals for the user
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId ORDER BY goal_date DESC")
    List<GoalEntity> getGoalsByUserId(int userId);
    
    /**
     * Get goal by user ID and date
     * @param userId User ID to filter by
     * @param goalDate Goal date timestamp
     * @return GoalEntity for the user on the specified date
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND goal_date = :goalDate")
    GoalEntity getGoalByUserAndDate(int userId, long goalDate);
    
    /**
     * Get goals by user ID and date range
     * @param userId User ID to filter by
     * @param startDate Start date timestamp
     * @param endDate End date timestamp
     * @return List of goals in date range
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND goal_date BETWEEN :startDate AND :endDate ORDER BY goal_date DESC")
    List<GoalEntity> getGoalsByUserAndDateRange(int userId, long startDate, long endDate);
    
    /**
     * Get achieved goals
     * @return List of achieved goals
     */
    @Query("SELECT * FROM goals WHERE is_achieved = 1 ORDER BY achieved_at DESC")
    List<GoalEntity> getAchievedGoals();
    
    /**
     * Get achieved goals by user
     * @param userId User ID to filter by
     * @return List of achieved goals for the user
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND is_achieved = 1 ORDER BY achieved_at DESC")
    List<GoalEntity> getAchievedGoalsByUser(int userId);
    
    /**
     * Get pending goals (not achieved)
     * @return List of pending goals
     */
    @Query("SELECT * FROM goals WHERE is_achieved = 0 ORDER BY goal_date ASC")
    List<GoalEntity> getPendingGoals();
    
    /**
     * Get pending goals by user
     * @param userId User ID to filter by
     * @return List of pending goals for the user
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND is_achieved = 0 ORDER BY goal_date ASC")
    List<GoalEntity> getPendingGoalsByUser(int userId);
    
    /**
     * Get goals for today
     * @param userId User ID to filter by
     * @param todayStart Start of today (timestamp)
     * @param todayEnd End of today (timestamp)
     * @return List of goals for today
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND goal_date BETWEEN :todayStart AND :todayEnd")
    List<GoalEntity> getTodayGoals(int userId, long todayStart, long todayEnd);
    
    /**
     * Get goals for current week
     * @param userId User ID to filter by
     * @param weekStart Start of week (timestamp)
     * @param weekEnd End of week (timestamp)
     * @return List of goals for current week
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND goal_date BETWEEN :weekStart AND :weekEnd ORDER BY goal_date ASC")
    List<GoalEntity> getWeekGoals(int userId, long weekStart, long weekEnd);
    
    /**
     * Get goals for current month
     * @param userId User ID to filter by
     * @param monthStart Start of month (timestamp)
     * @param monthEnd End of month (timestamp)
     * @return List of goals for current month
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND goal_date BETWEEN :monthStart AND :monthEnd ORDER BY goal_date ASC")
    List<GoalEntity> getMonthGoals(int userId, long monthStart, long monthEnd);
    
    /**
     * Get goals with high completion percentage
     * @param minPercentage Minimum completion percentage
     * @return List of goals with high completion percentage
     */
    @Query("SELECT * FROM goals WHERE completion_percentage >= :minPercentage ORDER BY completion_percentage DESC")
    List<GoalEntity> getGoalsWithHighCompletion(int minPercentage);
    
    /**
     * Get goals with high completion percentage by user
     * @param userId User ID to filter by
     * @param minPercentage Minimum completion percentage
     * @return List of goals with high completion percentage for the user
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND completion_percentage >= :minPercentage ORDER BY completion_percentage DESC")
    List<GoalEntity> getGoalsWithHighCompletionByUser(int userId, int minPercentage);
    
    /**
     * Get goals with low completion percentage
     * @param maxPercentage Maximum completion percentage
     * @return List of goals with low completion percentage
     */
    @Query("SELECT * FROM goals WHERE completion_percentage <= :maxPercentage AND is_achieved = 0 ORDER BY completion_percentage ASC")
    List<GoalEntity> getGoalsWithLowCompletion(int maxPercentage);
    
    /**
     * Get goals with low completion percentage by user
     * @param userId User ID to filter by
     * @param maxPercentage Maximum completion percentage
     * @return List of goals with low completion percentage for the user
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND completion_percentage <= :maxPercentage AND is_achieved = 0 ORDER BY completion_percentage ASC")
    List<GoalEntity> getGoalsWithLowCompletionByUser(int userId, int maxPercentage);
    
    /**
     * Get goal count by user
     * @param userId User ID to filter by
     * @return Number of goals for the user
     */
    @Query("SELECT COUNT(*) FROM goals WHERE user_id = :userId")
    int getGoalCountByUser(int userId);
    
    /**
     * Get achieved goal count by user
     * @param userId User ID to filter by
     * @return Number of achieved goals for the user
     */
    @Query("SELECT COUNT(*) FROM goals WHERE user_id = :userId AND is_achieved = 1")
    int getAchievedGoalCountByUser(int userId);
    
    /**
     * Get pending goal count by user
     * @param userId User ID to filter by
     * @return Number of pending goals for the user
     */
    @Query("SELECT COUNT(*) FROM goals WHERE user_id = :userId AND is_achieved = 0")
    int getPendingGoalCountByUser(int userId);
    
    /**
     * Get average completion percentage by user
     * @param userId User ID to filter by
     * @return Average completion percentage
     */
    @Query("SELECT COALESCE(AVG(completion_percentage), 0) FROM goals WHERE user_id = :userId")
    double getAverageCompletionPercentageByUser(int userId);
    
    /**
     * Get total target sessions by user
     * @param userId User ID to filter by
     * @return Total target sessions
     */
    @Query("SELECT COALESCE(SUM(target_sessions), 0) FROM goals WHERE user_id = :userId")
    int getTotalTargetSessionsByUser(int userId);
    
    /**
     * Get total completed sessions by user
     * @param userId User ID to filter by
     * @return Total completed sessions
     */
    @Query("SELECT COALESCE(SUM(completed_sessions), 0) FROM goals WHERE user_id = :userId")
    int getTotalCompletedSessionsByUser(int userId);
    
    /**
     * Get total target sessions by user for date range
     * @param userId User ID to filter by
     * @param startDate Start date timestamp
     * @param endDate End date timestamp
     * @return Total target sessions for the date range
     */
    @Query("SELECT COALESCE(SUM(target_sessions), 0) FROM goals WHERE user_id = :userId AND goal_date BETWEEN :startDate AND :endDate")
    int getTotalTargetSessionsByUserAndDateRange(int userId, long startDate, long endDate);
    
    /**
     * Get total completed sessions by user for date range
     * @param userId User ID to filter by
     * @param startDate Start date timestamp
     * @param endDate End date timestamp
     * @return Total completed sessions for the date range
     */
    @Query("SELECT COALESCE(SUM(completed_sessions), 0) FROM goals WHERE user_id = :userId AND goal_date BETWEEN :startDate AND :endDate")
    int getTotalCompletedSessionsByUserAndDateRange(int userId, long startDate, long endDate);
    
    /**
     * Get best completion percentage by user
     * @param userId User ID to filter by
     * @return Best completion percentage achieved
     */
    @Query("SELECT COALESCE(MAX(completion_percentage), 0) FROM goals WHERE user_id = :userId")
    int getBestCompletionPercentageByUser(int userId);
    
    /**
     * Get current streak of achieved goals by user
     * @param userId User ID to filter by
     * @param currentDate Current date timestamp
     * @return Current streak of achieved goals
     */
    @Query("SELECT COUNT(*) FROM goals WHERE user_id = :userId AND is_achieved = 1 AND goal_date <= :currentDate ORDER BY goal_date DESC")
    int getCurrentAchievementStreakByUser(int userId, long currentDate);
    
    // ==================== UPDATE OPERATIONS ====================
    
    /**
     * Update goal information
     * @param goal GoalEntity with updated information
     */
    @Update
    void updateGoal(GoalEntity goal);
    
    /**
     * Update goal progress
     * @param goalId Goal ID to update
     * @param completedSessions New completed sessions count
     * @param completionPercentage New completion percentage
     */
    @Query("UPDATE goals SET completed_sessions = :completedSessions, completion_percentage = :completionPercentage, updated_at = :updatedAt WHERE goal_id = :goalId")
    void updateGoalProgress(int goalId, int completedSessions, int completionPercentage, long updatedAt);
    
    /**
     * Mark goal as achieved
     * @param goalId Goal ID to mark as achieved
     * @param achievedAt Achievement timestamp
     */
    @Query("UPDATE goals SET is_achieved = 1, achieved_at = :achievedAt, updated_at = :updatedAt WHERE goal_id = :goalId")
    void markGoalAsAchieved(int goalId, long achievedAt, long updatedAt);
    
    /**
     * Mark goal as not achieved
     * @param goalId Goal ID to mark as not achieved
     */
    @Query("UPDATE goals SET is_achieved = 0, achieved_at = NULL, updated_at = :updatedAt WHERE goal_id = :goalId")
    void markGoalAsNotAchieved(int goalId, long updatedAt);
    
    /**
     * Update goal target sessions
     * @param goalId Goal ID to update
     * @param targetSessions New target sessions count
     */
    @Query("UPDATE goals SET target_sessions = :targetSessions, updated_at = :updatedAt WHERE goal_id = :goalId")
    void updateGoalTargetSessions(int goalId, int targetSessions, long updatedAt);
    
    /**
     * Increment completed sessions
     * @param goalId Goal ID to update
     */
    @Query("UPDATE goals SET completed_sessions = completed_sessions + 1, updated_at = :updatedAt WHERE goal_id = :goalId")
    void incrementCompletedSessions(int goalId, long updatedAt);
    
    /**
     * Decrement completed sessions
     * @param goalId Goal ID to update
     */
    @Query("UPDATE goals SET completed_sessions = completed_sessions - 1, updated_at = :updatedAt WHERE goal_id = :goalId")
    void decrementCompletedSessions(int goalId, long updatedAt);
    
    /**
     * Update completion percentage
     * @param goalId Goal ID to update
     * @param completionPercentage New completion percentage
     */
    @Query("UPDATE goals SET completion_percentage = :completionPercentage, updated_at = :updatedAt WHERE goal_id = :goalId")
    void updateCompletionPercentage(int goalId, int completionPercentage, long updatedAt);
    
    /**
     * Recalculate completion percentage based on completed and target sessions
     * @param goalId Goal ID to update
     */
    @Query("UPDATE goals SET completion_percentage = (completed_sessions * 100) / target_sessions, updated_at = :updatedAt WHERE goal_id = :goalId AND target_sessions > 0")
    void recalculateCompletionPercentage(int goalId, long updatedAt);
    
    /**
     * Update goal date
     * @param goalId Goal ID to update
     * @param newDate New goal date timestamp
     */
    @Query("UPDATE goals SET goal_date = :newDate, updated_at = :updatedAt WHERE goal_id = :goalId")
    void updateGoalDate(int goalId, long newDate, long updatedAt);
    
    // ==================== DELETE OPERATIONS ====================
    
    /**
     * Delete goal by ID
     * @param goal GoalEntity to delete
     */
    @Delete
    void deleteGoal(GoalEntity goal);
    
    /**
     * Delete goal by ID
     * @param goalId Goal ID to delete
     */
    @Query("DELETE FROM goals WHERE goal_id = :goalId")
    void deleteGoalById(int goalId);
    
    /**
     * Delete goals by user ID
     * @param userId User ID to filter by
     */
    @Query("DELETE FROM goals WHERE user_id = :userId")
    void deleteGoalsByUser(int userId);
    
    /**
     * Delete achieved goals
     */
    @Query("DELETE FROM goals WHERE is_achieved = 1")
    void deleteAchievedGoals();
    
    /**
     * Delete achieved goals by user
     * @param userId User ID to filter by
     */
    @Query("DELETE FROM goals WHERE user_id = :userId AND is_achieved = 1")
    void deleteAchievedGoalsByUser(int userId);
    
    /**
     * Delete goals older than specified date
     * @param timestamp Timestamp to compare
     */
    @Query("DELETE FROM goals WHERE created_at < :timestamp")
    void deleteGoalsOlderThan(long timestamp);
    
    /**
     * Delete all goals (use with caution)
     */
    @Query("DELETE FROM goals")
    void deleteAllGoals();
}
