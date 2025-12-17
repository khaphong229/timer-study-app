package com.example.timerstudy.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.timerstudy.data.local.database.entities.TaskEntity;

import java.util.List;

/**
 * Data Access Object for Task operations
 * Provides methods to interact with the tasks table
 */
@Dao
public interface TaskDao {
    
    // ==================== CREATE OPERATIONS ====================
    
    /**
     * Insert a new task
     * @param task TaskEntity to insert
     * @return The ID of the inserted task
     */
    @Insert
    long insertTask(TaskEntity task);
    
    /**
     * Insert multiple tasks
     * @param tasks List of TaskEntity to insert
     * @return List of inserted task IDs
     */
    @Insert
    List<Long> insertTasks(List<TaskEntity> tasks);
    
    // ==================== READ OPERATIONS ====================
    
    /**
     * Get all tasks
     * @return List of all tasks
     */
    @Query("SELECT * FROM tasks ORDER BY order_index ASC, created_at DESC")
    List<TaskEntity> getAllTasks();
    
    /**
     * Get task by ID
     * @param taskId Task ID to search for
     * @return TaskEntity or null if not found
     */
    @Query("SELECT * FROM tasks WHERE task_id = :taskId")
    TaskEntity getTaskById(int taskId);
    
    /**
     * Get tasks by user ID
     * @param userId User ID to filter by
     * @return List of tasks for the user
     */
    @Query("SELECT * FROM tasks WHERE user_id = :userId ORDER BY order_index ASC, created_at DESC")
    List<TaskEntity> getTasksByUserId(long userId);
    
    /**
     * Get tasks by user ID and date
     * @param userId User ID to filter by
     * @param taskDate Task date timestamp
     * @return List of tasks for the user on the specified date
     */
    @Query("SELECT * FROM tasks WHERE user_id = :userId AND task_date = :taskDate ORDER BY order_index ASC")
    List<TaskEntity> getTasksByUserAndDate(long userId, long taskDate);
    
    /**
     * Get tasks by user ID and date range
     * @param userId User ID to filter by
     * @param startDate Start date timestamp
     * @param endDate End date timestamp
     * @return List of tasks in date range
     */
    @Query("SELECT * FROM tasks WHERE user_id = :userId AND task_date BETWEEN :startDate AND :endDate ORDER BY task_date DESC, order_index ASC")
    List<TaskEntity> getTasksByUserAndDateRange(long userId, long startDate, long endDate);
    
    /**
     * Get tasks by priority
     * @param priority Priority level (HIGH, MEDIUM, LOW)
     * @return List of tasks with the specified priority
     */
    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY order_index ASC, created_at DESC")
    List<TaskEntity> getTasksByPriority(String priority);
    
    /**
     * Get tasks by user and priority
     * @param userId User ID to filter by
     * @param priority Priority level (HIGH, MEDIUM, LOW)
     * @return List of tasks for the user with the specified priority
     */
    @Query("SELECT * FROM tasks WHERE user_id = :userId AND priority = :priority ORDER BY order_index ASC, created_at DESC")
    List<TaskEntity> getTasksByUserAndPriority(long userId, String priority);
    
    /**
     * Get completed tasks
     * @return List of completed tasks
     */
    @Query("SELECT * FROM tasks WHERE is_completed = 1 ORDER BY completed_at DESC")
    List<TaskEntity> getCompletedTasks();
    
    /**
     * Get completed tasks by user
     * @param userId User ID to filter by
     * @return List of completed tasks for the user
     */
    @Query("SELECT * FROM tasks WHERE user_id = :userId AND is_completed = 1 ORDER BY completed_at DESC")
    List<TaskEntity> getCompletedTasksByUser(long userId);
    
    /**
     * Get pending tasks
     * @return List of pending (not completed) tasks
     */
    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY order_index ASC, created_at DESC")
    List<TaskEntity> getPendingTasks();
    
    /**
     * Get pending tasks by user
     * @param userId User ID to filter by
     * @return List of pending tasks for the user
     */
    @Query("SELECT * FROM tasks WHERE user_id = :userId AND is_completed = 0 ORDER BY order_index ASC, created_at DESC")
    List<TaskEntity> getPendingTasksByUser(long userId);
    
    /**
     * Get high priority tasks
     * @return List of high priority tasks
     */
    @Query("SELECT * FROM tasks WHERE priority = 'HIGH' AND is_completed = 0 ORDER BY order_index ASC, created_at DESC")
    List<TaskEntity> getHighPriorityTasks();
    
    /**
     * Get high priority tasks by user
     * @param userId User ID to filter by
     * @return List of high priority tasks for the user
     */
    @Query("SELECT * FROM tasks WHERE user_id = :userId AND priority = 'HIGH' AND is_completed = 0 ORDER BY order_index ASC, created_at DESC")
    List<TaskEntity> getHighPriorityTasksByUser(long userId);
    
    /**
     * Get tasks for today
     * @param userId User ID to filter by
     * @param todayStart Start of today (timestamp)
     * @param todayEnd End of today (timestamp)
     * @return List of tasks for today
     */
    @Query("SELECT * FROM tasks WHERE user_id = :userId AND task_date BETWEEN :todayStart AND :todayEnd ORDER BY order_index ASC")
    List<TaskEntity> getTodayTasks(long userId, long todayStart, long todayEnd);
    
    /**
     * Get overdue tasks (past due date and not completed)
     * @param currentTime Current timestamp
     * @return List of overdue tasks
     */
    @Query("SELECT * FROM tasks WHERE task_date < :currentTime AND is_completed = 0 ORDER BY task_date ASC")
    List<TaskEntity> getOverdueTasks(long currentTime);
    
    /**
     * Get overdue tasks by user
     * @param userId User ID to filter by
     * @param currentTime Current timestamp
     * @return List of overdue tasks for the user
     */
    @Query("SELECT * FROM tasks WHERE user_id = :userId AND task_date < :currentTime AND is_completed = 0 ORDER BY task_date ASC")
    List<TaskEntity> getOverdueTasksByUser(long userId, long currentTime);
    
    /**
     * Search tasks by title or description
     * @param searchQuery Search query
     * @return List of tasks matching the search query
     */
    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%' ORDER BY order_index ASC, created_at DESC")
    List<TaskEntity> searchTasks(String searchQuery);
    
    /**
     * Search tasks by user and query
     * @param userId User ID to filter by
     * @param searchQuery Search query
     * @return List of tasks for the user matching the search query
     */
    @Query("SELECT * FROM tasks WHERE user_id = :userId AND (title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%') ORDER BY order_index ASC, created_at DESC")
    List<TaskEntity> searchTasksByUser(long userId, String searchQuery);
    
    /**
     * Get task count by user
     * @param userId User ID to filter by
     * @return Number of tasks for the user
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE user_id = :userId")
    int getTaskCountByUser(long userId);
    
    /**
     * Get completed task count by user
     * @param userId User ID to filter by
     * @return Number of completed tasks for the user
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE user_id = :userId AND is_completed = 1")
    int getCompletedTaskCountByUser(long userId);
    
    /**
     * Get pending task count by user
     * @param userId User ID to filter by
     * @return Number of pending tasks for the user
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE user_id = :userId AND is_completed = 0")
    int getPendingTaskCountByUser(long userId);
    
    /**
     * Get total time spent on tasks by user
     * @param userId User ID to filter by
     * @return Total time spent in minutes
     */
    @Query("SELECT COALESCE(SUM(total_time_spent), 0) FROM tasks WHERE user_id = :userId")
    int getTotalTimeSpentByUser(long userId);
    
    /**
     * Get total time spent on completed tasks by user
     * @param userId User ID to filter by
     * @return Total time spent on completed tasks in minutes
     */
    @Query("SELECT COALESCE(SUM(total_time_spent), 0) FROM tasks WHERE user_id = :userId AND is_completed = 1")
    int getTotalTimeSpentOnCompletedTasksByUser(long userId);
    
    /**
     * Get average time spent per task by user
     * @param userId User ID to filter by
     * @return Average time spent per task in minutes
     */
    @Query("SELECT COALESCE(AVG(total_time_spent), 0) FROM tasks WHERE user_id = :userId AND total_time_spent > 0")
    double getAverageTimeSpentPerTaskByUser(long userId);
    
    // ==================== UPDATE OPERATIONS ====================
    
    /**
     * Update task information
     * @param task TaskEntity with updated information
     */
    @Update
    void updateTask(TaskEntity task);
    
    /**
     * Mark task as completed
     * @param taskId Task ID to complete
     * @param completedAt Completion timestamp
     */
    @Query("UPDATE tasks SET is_completed = 1, completed_at = :completedAt, updated_at = :updatedAt WHERE task_id = :taskId")
    void completeTask(int taskId, long completedAt, long updatedAt);
    
    /**
     * Mark task as pending
     * @param taskId Task ID to mark as pending
     */
    @Query("UPDATE tasks SET is_completed = 0, completed_at = NULL, updated_at = :updatedAt WHERE task_id = :taskId")
    void markTaskAsPending(int taskId, long updatedAt);
    
    /**
     * Update task priority
     * @param taskId Task ID to update
     * @param priority New priority level
     */
    @Query("UPDATE tasks SET priority = :priority, updated_at = :updatedAt WHERE task_id = :taskId")
    void updateTaskPriority(int taskId, String priority, long updatedAt);
    
    /**
     * Update task order index
     * @param taskId Task ID to update
     * @param orderIndex New order index
     */
    @Query("UPDATE tasks SET order_index = :orderIndex, updated_at = :updatedAt WHERE task_id = :taskId")
    void updateTaskOrder(int taskId, int orderIndex, long updatedAt);
    
    /**
     * Update task time spent
     * @param taskId Task ID to update
     * @param timeSpent Time spent in minutes
     */
    @Query("UPDATE tasks SET total_time_spent = :timeSpent, updated_at = :updatedAt WHERE task_id = :taskId")
    void updateTaskTimeSpent(int taskId, int timeSpent, long updatedAt);
    
    /**
     * Add time to task
     * @param taskId Task ID to update
     * @param additionalTime Additional time in minutes
     */
    @Query("UPDATE tasks SET total_time_spent = total_time_spent + :additionalTime, updated_at = :updatedAt WHERE task_id = :taskId")
    void addTimeToTask(int taskId, int additionalTime, long updatedAt);
    
    /**
     * Update task actual sessions
     * @param taskId Task ID to update
     * @param actualSessions New actual sessions count
     */
    @Query("UPDATE tasks SET actual_sessions = :actualSessions, updated_at = :updatedAt WHERE task_id = :taskId")
    void updateTaskActualSessions(int taskId, int actualSessions, long updatedAt);
    
    /**
     * Increment task actual sessions
     * @param taskId Task ID to update
     */
    @Query("UPDATE tasks SET actual_sessions = actual_sessions + 1, updated_at = :updatedAt WHERE task_id = :taskId")
    void incrementTaskActualSessions(int taskId, long updatedAt);
    
    /**
     * Update task date
     * @param taskId Task ID to update
     * @param newDate New task date timestamp
     */
    @Query("UPDATE tasks SET task_date = :newDate, updated_at = :updatedAt WHERE task_id = :taskId")
    void updateTaskDate(int taskId, long newDate, long updatedAt);
    
    // ==================== DELETE OPERATIONS ====================
    
    /**
     * Delete task by ID
     * @param task TaskEntity to delete
     */
    @Delete
    void deleteTask(TaskEntity task);
    
    /**
     * Delete task by ID
     * @param taskId Task ID to delete
     */
    @Query("DELETE FROM tasks WHERE task_id = :taskId")
    void deleteTaskById(int taskId);

    /**
     * Delete tasks by user IDghe
     * @param userId User ID to filter by
     */
    @Query("DELETE FROM tasks WHERE user_id = :userId")
    void deleteTasksByUser(long userId);
    
    /**
     * Delete completed tasks
     */
    @Query("DELETE FROM tasks WHERE is_completed = 1")
    void deleteCompletedTasks();
    
    /**
     * Delete completed tasks by user
     * @param userId User ID to filter by
     */
    @Query("DELETE FROM tasks WHERE user_id = :userId AND is_completed = 1")
    void deleteCompletedTasksByUser(long userId);
    
    /**
     * Delete tasks older than specified date
     * @param timestamp Timestamp to compare
     */
    @Query("DELETE FROM tasks WHERE created_at < :timestamp")
    void deleteTasksOlderThan(long timestamp);
    
    /**
     * Delete all tasks (use with caution)
     */
    @Query("DELETE FROM tasks")
    void deleteAllTasks();
}
