package com.example.timerstudy.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timerstudy.data.local.database.AppDatabase;
import com.example.timerstudy.data.local.database.dao.SessionDao;
import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.example.timerstudy.data.remote.RetrofitClient;
import com.example.timerstudy.data.remote.ApiService;
import com.example.timerstudy.model.Session;
import com.example.timerstudy.utils.UserManager;

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
    private final Context context;
    
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
        this.context = context;
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
    public void loadSessionsByUserId(long userId) {
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
    public void loadSessionsByUserAndDateRange(long userId, long startDate, long endDate) {
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
    public void startFocusSession(long userId, int durationMinutes) {
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
    public void startBreakSession(long userId, int durationMinutes, boolean isLongBreak) {
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
    public void getSessionsInProgressByUser(long userId) {
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
    public void getCompletedSessionsByUser(long userId) {
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
    public void getTodaySessions(long userId) {
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
    public void getTotalFocusTimeByUser(long userId) {
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
    public void loadCompletedSessionsCountToday(long userId) {
        executorService.execute(() -> {
            try {
                Log.d("DebugLoadCompletedSession", "userId" + userId);
                Log.d("DebugLoadCompletedSession", "loadCompletedSessionsCountToday: ");
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
                calendar.set(java.util.Calendar.MINUTE, 0);
                calendar.set(java.util.Calendar.SECOND, 0);
                calendar.set(java.util.Calendar.MILLISECOND, 0);
                long todayStart = calendar.getTimeInMillis();
                java.util.Date todayStartDate = calendar.getTime();
                int count = sessionDao.countCompletedFocusSessionsSince(userId, todayStartDate);
                
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
    public void loadTotalCompletedSessionsCount(long userId) {
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
    public void saveCompletedStudySession(long userId, int durationMinutes) {
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
                session.setSynced(false); // Mặc định chưa sync
                
                long sessionId = sessionDao.insertSession(session);
                session.setSessionId((int) sessionId); // Update ID
                
                Log.d(TAG, "Saved completed study session: " + sessionId);
        
                loadCompletedSessionsCountToday(userId);
                loadSessionsByUserId(userId);
                errorLiveData.postValue(null);

                Session sessionModel = new Session();
                sessionModel.setSession_id(session.getSessionId());
                sessionModel.setUser_id(session.getUserId());
                sessionModel.setSession_date(session.getSessionDate() != null ? session.getSessionDate().getTime() : null);
                sessionModel.setStart_time(session.getStartTime() != null ? session.getStartTime().getTime() : null);
                sessionModel.setEnd_time(session.getEndTime() != null ? session.getEndTime().getTime() : null);
                sessionModel.setDuration_minutes(session.getDurationMinutes());
                sessionModel.setActual_duration_minutes(session.getActualDurationMinutes());
                sessionModel.setSession_type(session.getSessionType());
                sessionModel.setStatus(session.getStatus());
                sessionModel.setFocus_session_count(0); // Default or from entity
                sessionModel.setIs_completed(session.isCompleted() ? 1 : 0);
                sessionModel.setPause_count(session.getPauseCount());
                sessionModel.setTotal_pause_duration(session.getTotalPauseDuration());
                // sessionModel.setCreated_at(session.getCreatedAt()); // CreatedAt is String in model but Date in Entity, handle conversion if needed or let server set it

                UserManager.getInstance(context).syncSession(sessionModel);

            } catch (Exception e) {
                Log.e(TAG, "Error saving completed study session", e);
                errorLiveData.postValue("Failed to save session: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }


    public void syncSession(Session session, String accessToken, SyncCallback callback) {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
  
        String authHeader = accessToken.startsWith("Bearer ") ? accessToken : "Bearer " + accessToken;
        Log.d("FixSyncSession", "authHeader" + authHeader);

        apiService.createSession(authHeader, session).enqueue(new Callback<ApiService.ApiResponse<Session>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<Session>> call, Response<ApiService.ApiResponse<Session>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Session serverSession = response.body().data;
                    
                    Log.d("FixSyncSession", "Sync success. Local ID: " + session.getSession_id() + " -> Server ID: " + serverSession.getSession_id());

                    executorService.execute(() -> {
                        SessionEntity entity = sessionDao.getSessionById(session.getSession_id());
                        if (entity != null) {
                            entity.setSynced(true);
                            sessionDao.updateSession(entity);
                        }
                    });
                    
                    if (callback != null) callback.onSuccess();
                } else {
                    String errorMsg = "Server error: " + response.code();
                    if (response.body() != null) errorMsg += " - " + response.body().message;
                    
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " | " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("FixSyncSession", "Error reading error body", e);
                    }
                    Log.e("FixSyncSession", errorMsg);

                    if (callback != null) callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<Session>> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e("FixSyncSession", errorMsg, t);
                if (callback != null) callback.onError(errorMsg);
            }
        });
    }

    // Hàm đồng bộ session lên server (Cho SessionEntity)
    public void syncSession(SessionEntity sessionEntity, String accessToken, SyncCallback callback) {
        // Convert Entity to Model
        com.example.timerstudy.model.Session session = new com.example.timerstudy.model.Session();
        session.setSession_id(sessionEntity.getSessionId());
        session.setUser_id(sessionEntity.getUserId());
        session.setSession_date(sessionEntity.getSessionDate() != null ? sessionEntity.getSessionDate().getTime() : null);
        session.setStart_time(sessionEntity.getStartTime() != null ? sessionEntity.getStartTime().getTime() : null);
        session.setEnd_time(sessionEntity.getEndTime() != null ? sessionEntity.getEndTime().getTime() : null);
        session.setDuration_minutes(sessionEntity.getDurationMinutes());
        session.setActual_duration_minutes(sessionEntity.getActualDurationMinutes());
        session.setSession_type(sessionEntity.getSessionType());
        session.setStatus(sessionEntity.getStatus());
        session.setFocus_session_count(0); // Default
        session.setIs_completed(sessionEntity.isCompleted() ? 1 : 0);
        session.setPause_count(sessionEntity.getPauseCount());
        session.setTotal_pause_duration(sessionEntity.getTotalPauseDuration());
        // session.setCreated_at(sessionEntity.getCreatedAt());

        syncSession(session, accessToken, callback);
    }

    /**
     * Synchronous sync for WorkManager
     * @param sessionEntity Session to sync
     * @param accessToken Auth token
     * @return true if success
     */
    public boolean syncSessionSynchronous(SessionEntity sessionEntity, String accessToken) {
        try {
            // Convert Entity to Model
            com.example.timerstudy.model.Session session = new com.example.timerstudy.model.Session();
            session.setSession_id(sessionEntity.getSessionId());
            session.setUser_id(sessionEntity.getUserId());
            session.setSession_date(sessionEntity.getSessionDate() != null ? sessionEntity.getSessionDate().getTime() : null);
            session.setStart_time(sessionEntity.getStartTime() != null ? sessionEntity.getStartTime().getTime() : null);
            session.setEnd_time(sessionEntity.getEndTime() != null ? sessionEntity.getEndTime().getTime() : null);
            session.setDuration_minutes(sessionEntity.getDurationMinutes());
            session.setActual_duration_minutes(sessionEntity.getActualDurationMinutes());
            session.setSession_type(sessionEntity.getSessionType());
            session.setStatus(sessionEntity.getStatus());
            session.setFocus_session_count(0);
            session.setIs_completed(sessionEntity.isCompleted() ? 1 : 0);
            session.setPause_count(sessionEntity.getPauseCount());
            session.setTotal_pause_duration(sessionEntity.getTotalPauseDuration());
            // session.setCreated_at(sessionEntity.getCreatedAt());

            ApiService apiService = RetrofitClient.getInstance().getApiService();
            String authHeader = "Bearer " + accessToken;

            Response<ApiService.ApiResponse<com.example.timerstudy.model.Session>> response = 
                apiService.createSession(authHeader, session).execute();

            if (response.isSuccessful() && response.body() != null && response.body().success) {
                // Update local status
                sessionEntity.setSynced(true);
                sessionDao.updateSession(sessionEntity);
                return true;
            } else {
                Log.e(TAG, "Sync failed: " + response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "Sync failed for session " + sessionEntity.getSessionId(), e);
        }
        return false;
    }

    /**
     * Sync all pending sessions (is_synced = 0)
     * Call this when internet is available
     */
    public void syncPendingSessions(String accessToken) {
        executorService.execute(() -> {
            List<SessionEntity> pendingSessions = sessionDao.getUnsyncedSessions();
            if (pendingSessions != null && !pendingSessions.isEmpty()) {
                Log.d(TAG, "Found " + pendingSessions.size() + " pending sessions to sync");
                for (SessionEntity entity : pendingSessions) {
                    syncSession(entity, accessToken, new SyncCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Synced pending session: " + entity.getSessionId());
                        }

                        @Override
                        public void onError(String message) {
                            Log.e(TAG, "Failed to sync pending session " + entity.getSessionId() + ": " + message);
                        }
                    });
                }
            }
        });
    }

    public List<SessionEntity> getAllSessionsSync() {
        return sessionDao.getAllSessions();
    }

    public List<SessionEntity> getUnsyncedSessionsSync() {
        return sessionDao.getUnsyncedSessions();
    }

    public interface SyncCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Fetch all sessions from API and save to local DB
     * @param accessToken User access token
     * @param userId User ID to associate sessions with
     */
    public void fetchAndSaveSessionsFromApi(String accessToken, long userId) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                ApiService apiService = RetrofitClient.getInstance().getApiService();
                String authHeader = "Bearer " + accessToken;

                Call<ApiService.ApiResponse<List<com.example.timerstudy.model.Session>>> call = apiService.getAllSessions(authHeader);
                Response<ApiService.ApiResponse<List<com.example.timerstudy.model.Session>>> response = call.execute();

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<com.example.timerstudy.model.Session> serverSessions = response.body().data;
                    if (serverSessions != null) {
                        Log.d(TAG, "Fetched " + serverSessions.size() + " sessions from server");
                        
                        for (com.example.timerstudy.model.Session serverSession : serverSessions) {
                            // Check if session exists
                            SessionEntity existingSession = sessionDao.getSessionById(serverSession.getSession_id());
                            
                            SessionEntity entity = new SessionEntity();
                            entity.setSessionId(serverSession.getSession_id());
                            entity.setUserId(userId); // Ensure it maps to current user
                            entity.setSessionDate(serverSession.getSession_date() != null ? new java.util.Date(serverSession.getSession_date()) : null);
                            entity.setStartTime(serverSession.getStart_time() != null ? new java.util.Date(serverSession.getStart_time()) : null);
                            entity.setEndTime(serverSession.getEnd_time() != null ? new java.util.Date(serverSession.getEnd_time()) : null);
                            entity.setDurationMinutes(serverSession.getDuration_minutes() != null ? serverSession.getDuration_minutes() : 0);
                            entity.setActualDurationMinutes(serverSession.getActual_duration_minutes() != null ? serverSession.getActual_duration_minutes() : 0);
                            entity.setSessionType(serverSession.getSession_type());
                            entity.setStatus(serverSession.getStatus());
                            // entity.setCreatedAt(serverSession.getCreated_at()); // Handle String to Date conversion if needed
                            entity.setUpdatedAt(new java.util.Date());
                            entity.setSynced(true); // It came from server, so it is synced
                            entity.setCompleted(serverSession.getIs_completed() != null && serverSession.getIs_completed() == 1);
                            entity.setPauseCount(serverSession.getPause_count() != null ? serverSession.getPause_count() : 0);
                            entity.setTotalPauseDuration(serverSession.getTotal_pause_duration() != null ? serverSession.getTotal_pause_duration() : 0);

                            if (existingSession == null) {
                                sessionDao.insertSession(entity);
                            } else {
                                sessionDao.updateSession(entity);
                            }
                        }
                        
                        // Refresh UI
                        loadSessionsByUserId(userId);
                        loadCompletedSessionsCountToday(userId);
                    }
                } else {
                    Log.e(TAG, "Failed to fetch sessions: " + response.code());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching sessions", e);
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
}
