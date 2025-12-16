package com.example.timerstudy.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timerstudy.data.local.database.AppDatabase;
import com.example.timerstudy.data.local.database.dao.SessionDao;
import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.example.timerstudy.data.remote.ApiClient;
import com.example.timerstudy.data.remote.ApiService;
import com.example.timerstudy.model.Session;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for Session data operations
 * 
 * This repository handles all session-related database operations and provides
 * a clean interface for the ViewModel layer. It manages background threads
 * and error handling for database operations.
 */
public class SessionRepository {
    
    private static final String TAG = "SessionRepository";
    
    // Database and DAO
    private final AppDatabase database;
    private final SessionDao sessionDao;
    
    // Thread executor for background operations
    private final ExecutorService executorService;
    
    // LiveData for reactive updates
    private final MutableLiveData<List<SessionEntity>> allSessionsLiveData;
    private final MutableLiveData<List<SessionEntity>> userSessionsLiveData;
    private final MutableLiveData<SessionEntity> currentSessionLiveData;
    private final MutableLiveData<Integer> completedSessionsCountLiveData;
    private final MutableLiveData<Boolean> isLoadingLiveData;
    private final MutableLiveData<String> errorLiveData;
    
    // Singleton instance
    private static volatile SessionRepository INSTANCE;
    
    /**
     * Private constructor for Singleton pattern
     * 
     * @param context Application context
     */
    private SessionRepository(Context context) {
        database = AppDatabase.getDatabase(context);
        sessionDao = database.sessionDao();
        executorService = Executors.newFixedThreadPool(4);
        
        // Initialize LiveData
        allSessionsLiveData = new MutableLiveData<>();
        userSessionsLiveData = new MutableLiveData<>();
        currentSessionLiveData = new MutableLiveData<>();
        completedSessionsCountLiveData = new MutableLiveData<>(); // THÊM dòng này
        isLoadingLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
    }
    
    /**
     * Get singleton instance
     * 
     * @param context Application context
     * @return SessionRepository instance
     */
    public static SessionRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SessionRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SessionRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }
    
    // ==================== LIVE DATA GETTERS ====================
    
    /**
     * Get all sessions LiveData
     * 
     * @return LiveData containing list of all sessions
     */
    public LiveData<List<SessionEntity>> getAllSessionsLiveData() {
        return allSessionsLiveData;
    }
    
    /**
     * Get user sessions LiveData
     * 
     * @return LiveData containing list of user sessions
     */
    public LiveData<List<SessionEntity>> getUserSessionsLiveData() {
        return userSessionsLiveData;
    }
    
    /**
     * Get current session LiveData
     * 
     * @return LiveData containing current session
     */
    public LiveData<SessionEntity> getCurrentSessionLiveData() {
        return currentSessionLiveData;
    }
    
    /**
     * Get loading state LiveData
     * 
     * @return LiveData containing loading state
     */
    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }
    
    /**
     * Get error LiveData
     * 
     * @return LiveData containing error messages
     */
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
    
    /**
     * Get completed sessions count LiveData
     * 
     * @return LiveData containing count of completed sessions
     */
    public LiveData<Integer> getCompletedSessionsCountLiveData() {
        return completedSessionsCountLiveData;
    }
    
    // ==================== SESSION OPERATIONS ====================
    
    /**
     * Load all sessions from database
     */
    public void loadAllSessions() {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                List<SessionEntity> sessions = sessionDao.getAllSessions();
                allSessionsLiveData.postValue(sessions);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading all sessions", e);
                errorLiveData.postValue("Failed to load sessions: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    /**
     * Load sessions by user ID
     * 
     * @param userId User ID to filter by
     */
    public void loadSessionsByUserId(int userId) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                List<SessionEntity> sessions = sessionDao.getSessionsByUserId(userId);
                userSessionsLiveData.postValue(sessions);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading sessions for user: " + userId, e);
                errorLiveData.postValue("Failed to load user sessions: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    /**
     * Load sessions by user ID and date range
     * 
     * @param userId User ID to filter by
     * @param startDate Start date timestamp
     * @param endDate End date timestamp
     */
    public void loadSessionsByUserAndDateRange(int userId, long startDate, long endDate) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                List<SessionEntity> sessions = sessionDao.getSessionsByUserAndDateRange(userId, startDate, endDate);
                userSessionsLiveData.postValue(sessions);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading sessions for user and date range: " + userId, e);
                errorLiveData.postValue("Failed to load sessions: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    /**
     * Get session by ID
     * 
     * @param sessionId Session ID to search for
     */
    public void getSessionById(int sessionId) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                SessionEntity session = sessionDao.getSessionById(sessionId);
                currentSessionLiveData.postValue(session);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error getting session by ID: " + sessionId, e);
                errorLiveData.postValue("Failed to get session: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    /**
     * Create a new session
     * 
     * @param session SessionEntity to create
     */
    public void createSession(SessionEntity session) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                long sessionId = sessionDao.insertSession(session);
                session.setSessionId((int) sessionId);
                currentSessionLiveData.postValue(session);
                loadSessionsByUserId(session.getUserId()); // Refresh user sessions
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error creating session", e);
                errorLiveData.postValue("Failed to create session: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    /**
     * Start a new focus session
     * 
     * @param userId User ID
     * @param durationMinutes Session duration in minutes
     */
    public void startFocusSession(int userId, int durationMinutes) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                SessionEntity session = new SessionEntity();
                session.setUserId(userId);
                session.setSessionType(SessionEntity.TYPE_FOCUS_SESSION);
                session.setDurationMinutes(durationMinutes);
                session.setStartTime(new java.util.Date());
                session.setSessionDate(new java.util.Date());
                session.setStatus(SessionEntity.STATUS_IN_PROGRESS);
                
                long sessionId = sessionDao.insertSession(session);
                session.setSessionId((int) sessionId);
                currentSessionLiveData.postValue(session);
                loadSessionsByUserId(userId); // Refresh user sessions
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error starting focus session", e);
                errorLiveData.postValue("Failed to start focus session: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    /**
     * Start a new break session
     * 
     * @param userId User ID
     * @param durationMinutes Break duration in minutes
     * @param isLongBreak True for long break, false for short break
     */
    public void startBreakSession(int userId, int durationMinutes, boolean isLongBreak) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                SessionEntity session = new SessionEntity();
                session.setUserId(userId);
                session.setSessionType(isLongBreak ? SessionEntity.TYPE_LONG_BREAK : SessionEntity.TYPE_SHORT_BREAK);
                session.setDurationMinutes(durationMinutes);
                session.setStartTime(new java.util.Date());
                session.setSessionDate(new java.util.Date());
                session.setStatus(SessionEntity.STATUS_IN_PROGRESS);
                
                long sessionId = sessionDao.insertSession(session);
                session.setSessionId((int) sessionId);
                currentSessionLiveData.postValue(session);
                loadSessionsByUserId(userId); // Refresh user sessions
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error starting break session", e);
                errorLiveData.postValue("Failed to start break session: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    /**
     * Update session information
     * 
     * @param session SessionEntity with updated information
     */
    public void updateSession(SessionEntity session) {
        executorService.execute(() -> {
            try {
                session.setUpdatedAt(new java.util.Date());
                sessionDao.updateSession(session);
                currentSessionLiveData.postValue(session);
                loadSessionsByUserId(session.getUserId()); // Refresh user sessions
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error updating session", e);
                errorLiveData.postValue("Failed to update session: " + e.getMessage());
            }
        });
    }
    
    /**
     * Complete a session
     * 
     * @param sessionId Session ID to complete
     * @param actualDuration Actual duration in minutes
     */
    public void completeSession(int sessionId, int actualDuration) {
        executorService.execute(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                sessionDao.completeSession(sessionId, currentTime, actualDuration, currentTime);
                
                // Update current session if it's the same session
                SessionEntity currentSession = currentSessionLiveData.getValue();
                if (currentSession != null && currentSession.getSessionId() == sessionId) {
                    currentSession.setStatus(SessionEntity.STATUS_COMPLETED);
                    currentSession.setEndTime(new java.util.Date(currentTime));
                    currentSession.setActualDurationMinutes(actualDuration);
                    currentSession.setCompleted(true);
                    currentSession.setUpdatedAt(new java.util.Date(currentTime));
                    currentSessionLiveData.postValue(currentSession);
                }
                
                // Refresh user sessions
                if (currentSession != null) {
                    loadSessionsByUserId(currentSession.getUserId());
                }
                
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error completing session: " + sessionId, e);
                errorLiveData.postValue("Failed to complete session: " + e.getMessage());
            }
        });
    }
    
    /**
     * Pause a session
     * 
     * @param sessionId Session ID to pause
     * @param pauseCount New pause count
     * @param totalPauseDuration Total pause duration in minutes
     */
    public void pauseSession(int sessionId, int pauseCount, int totalPauseDuration) {
        executorService.execute(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                sessionDao.pauseSession(sessionId, pauseCount, totalPauseDuration, currentTime);
                
                // Update current session if it's the same session
                SessionEntity currentSession = currentSessionLiveData.getValue();
                if (currentSession != null && currentSession.getSessionId() == sessionId) {
                    currentSession.setStatus(SessionEntity.STATUS_PAUSED);
                    currentSession.setPauseCount(pauseCount);
                    currentSession.setTotalPauseDuration(totalPauseDuration);
                    currentSession.setUpdatedAt(new java.util.Date(currentTime));
                    currentSessionLiveData.postValue(currentSession);
                }
                
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error pausing session: " + sessionId, e);
                errorLiveData.postValue("Failed to pause session: " + e.getMessage());
            }
        });
    }
    
    /**
     * Resume a session
     * 
     * @param sessionId Session ID to resume
     */
    public void resumeSession(int sessionId) {
        executorService.execute(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                sessionDao.resumeSession(sessionId, currentTime);
                
                // Update current session if it's the same session
                SessionEntity currentSession = currentSessionLiveData.getValue();
                if (currentSession != null && currentSession.getSessionId() == sessionId) {
                    currentSession.setStatus(SessionEntity.STATUS_IN_PROGRESS);
                    currentSession.setUpdatedAt(new java.util.Date(currentTime));
                    currentSessionLiveData.postValue(currentSession);
                }
                
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error resuming session: " + sessionId, e);
                errorLiveData.postValue("Failed to resume session: " + e.getMessage());
            }
        });
    }
    
    /**
     * Cancel a session
     * 
     * @param sessionId Session ID to cancel
     */
    public void cancelSession(int sessionId) {
        executorService.execute(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                sessionDao.cancelSession(sessionId, currentTime);
                
                // Update current session if it's the same session
                SessionEntity currentSession = currentSessionLiveData.getValue();
                if (currentSession != null && currentSession.getSessionId() == sessionId) {
                    currentSession.setStatus(SessionEntity.STATUS_CANCELLED);
                    currentSession.setUpdatedAt(new java.util.Date(currentTime));
                    currentSessionLiveData.postValue(currentSession);
                }
                
                // Refresh user sessions
                if (currentSession != null) {
                    loadSessionsByUserId(currentSession.getUserId());
                }
                
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error cancelling session: " + sessionId, e);
                errorLiveData.postValue("Failed to cancel session: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get sessions in progress for a user
     * 
     * @param userId User ID to filter by
     */
    public void getSessionsInProgressByUser(int userId) {
        executorService.execute(() -> {
            try {
                List<SessionEntity> sessions = sessionDao.getSessionsInProgressByUser(userId);
                userSessionsLiveData.postValue(sessions);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error getting sessions in progress for user: " + userId, e);
                errorLiveData.postValue("Failed to get sessions in progress: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get completed sessions for a user
     * 
     * @param userId User ID to filter by
     */
    public void getCompletedSessionsByUser(int userId) {
        executorService.execute(() -> {
            try {
                List<SessionEntity> sessions = sessionDao.getCompletedSessionsByUser(userId);
                userSessionsLiveData.postValue(sessions);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error getting completed sessions for user: " + userId, e);
                errorLiveData.postValue("Failed to get completed sessions: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get today's sessions for a user
     * 
     * @param userId User ID to filter by
     */
    public void getTodaySessions(int userId) {
        executorService.execute(() -> {
            try {
                // Calculate today's start and end timestamps
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
                calendar.set(java.util.Calendar.MINUTE, 0);
                calendar.set(java.util.Calendar.SECOND, 0);
                calendar.set(java.util.Calendar.MILLISECOND, 0);
                long todayStart = calendar.getTimeInMillis();
                
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
                long todayEnd = calendar.getTimeInMillis();
                
                List<SessionEntity> sessions = sessionDao.getTodaySessions(userId, todayStart, todayEnd);
                userSessionsLiveData.postValue(sessions);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error getting today's sessions for user: " + userId, e);
                errorLiveData.postValue("Failed to get today's sessions: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get total focus time for a user
     * 
     * @param userId User ID to filter by
     */
    public void getTotalFocusTimeByUser(int userId) {
        executorService.execute(() -> {
            try {
                int totalFocusTime = sessionDao.getTotalFocusTimeByUser(userId);
                Log.d(TAG, "Total focus time for user " + userId + ": " + totalFocusTime + " minutes");
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error getting total focus time for user: " + userId, e);
                errorLiveData.postValue("Failed to get total focus time: " + e.getMessage());
            }
        });
    }
    
    /**
     * Delete session by ID
     * 
     * @param sessionId Session ID to delete
     */
    public void deleteSession(int sessionId) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                sessionDao.deleteSessionById(sessionId);
                
                // Clear current session if it's the same session
                SessionEntity currentSession = currentSessionLiveData.getValue();
                if (currentSession != null && currentSession.getSessionId() == sessionId) {
                    currentSessionLiveData.postValue(null);
                }
                
                // Refresh user sessions
                if (currentSession != null) {
                    loadSessionsByUserId(currentSession.getUserId());
                }
                
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting session: " + sessionId, e);
                errorLiveData.postValue("Failed to delete session: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Clear all data
     */
    public void clearAllData() {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                sessionDao.deleteAllSessions();
                allSessionsLiveData.postValue(null);
                userSessionsLiveData.postValue(null);
                currentSessionLiveData.postValue(null);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error clearing all data", e);
                errorLiveData.postValue("Failed to clear data: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    /**
     * Close repository and cleanup resources
     */
    public void close() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    /**
     * Check if repository is closed
     * 
     * @return True if closed, false otherwise
     */
    public boolean isClosed() {
        return executorService == null || executorService.isShutdown();
    }

    /**
     * Load completed sessions count for today
     * 
     * @param userId User ID to filter by
     */
    public void loadCompletedSessionsCountToday(int userId) {
        executorService.execute(() -> {
            try {
                List<SessionEntity> sessions = sessionDao.getCompletedSessionsByUser(userId);
                
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
                calendar.set(java.util.Calendar.MINUTE, 0);
                calendar.set(java.util.Calendar.SECOND, 0);
                calendar.set(java.util.Calendar.MILLISECOND, 0);
                long todayStart = calendar.getTimeInMillis();
                
                int count = 0;
                for (SessionEntity session : sessions) {
                    if (session.getSessionDate() != null 
                        && session.getSessionDate().getTime() >= todayStart
                        && SessionEntity.TYPE_FOCUS_SESSION.equals(session.getSessionType())) {
                        count++;
                    }
                }
                
                completedSessionsCountLiveData.postValue(count);
                Log.d(TAG, "Today's completed sessions count: " + count);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading completed sessions count", e);
                errorLiveData.postValue("Failed to load count: " + e.getMessage());
            }
        });
    }
    
    /**
     * Load ALL completed sessions count (not just today)
     * For calculating total coins in Shop
     * 
     * @param userId User ID to filter by
     */
    public void loadTotalCompletedSessionsCount(int userId) {
        executorService.execute(() -> {
            try {
                List<SessionEntity> sessions = sessionDao.getCompletedSessionsByUser(userId);
                
                int count = 0;
                for (SessionEntity session : sessions) {
                    if (SessionEntity.TYPE_FOCUS_SESSION.equals(session.getSessionType())) {
                        count++;
                    }
                }
                
                completedSessionsCountLiveData.postValue(count);
                Log.d(TAG, "Total completed sessions count: " + count);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading total completed sessions count", e);
                errorLiveData.postValue("Failed to load count: " + e.getMessage());
            }
        });
    }
    
    /**
     * Save completed study session
     * 
     * @param userId User ID
     * @param durationMinutes Duration in minutes
     */
    public void saveCompletedStudySession(int userId, int durationMinutes) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                
                java.util.Date now = new java.util.Date();
                
                SessionEntity session = new SessionEntity();
                session.setUserId(userId);
                session.setSessionType(SessionEntity.TYPE_FOCUS_SESSION);
                session.setDurationMinutes(durationMinutes);
                session.setActualDurationMinutes(durationMinutes);
                session.setStartTime(now);
                session.setSessionDate(now);
                session.setEndTime(now);
                session.setStatus(SessionEntity.STATUS_COMPLETED);
                session.setCompleted(true);
                session.setCreatedAt(now);
                session.setUpdatedAt(now);
                
                long sessionId = sessionDao.insertSession(session);
                
                Log.d(TAG, "Saved completed study session: " + sessionId);
        
                loadCompletedSessionsCountToday(userId);
                loadSessionsByUserId(userId);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error saving completed study session", e);
                errorLiveData.postValue("Failed to save session: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    // Hàm đồng bộ session lên server
    public void syncSession(SessionEntity sessionEntity, String firebaseToken, SyncCallback callback) {
        ApiService apiService = ApiClient.getService();
        String authHeader = "Bearer " + firebaseToken;

        // Convert Entity to Model
        com.example.timerstudy.model.Session session = new com.example.timerstudy.model.Session();
        session.setSession_id(sessionEntity.getSessionId());
        session.setUser_id(sessionEntity.getUserId());
        session.setSession_date(sessionEntity.getSessionDate());
        session.setStart_time(sessionEntity.getStartTime());
        session.setEnd_time(sessionEntity.getEndTime());
        session.setDuration_minutes(sessionEntity.getDurationMinutes());
        session.setSession_type(sessionEntity.getSessionType());
        session.setStatus(sessionEntity.getStatus());
        session.setCreated_at(sessionEntity.getCreatedAt());

        apiService.createSession(authHeader, session).enqueue(new Callback<com.example.timerstudy.model.Session>() {
            @Override
            public void onResponse(Call<com.example.timerstudy.model.Session> call, Response<com.example.timerstudy.model.Session> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Server trả về session đã tạo (có thể có ID từ server)
                    // Bạn có thể update lại ID local nếu cần, hoặc chỉ cần log thành công
                    callback.onSuccess();
                } else {
                    callback.onError("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Session> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public List<SessionEntity> getAllSessionsSync() {
        return sessionDao.getAllSessions();
    }

    public interface SyncCallback {
        void onSuccess();
        void onError(String message);
    }
}
