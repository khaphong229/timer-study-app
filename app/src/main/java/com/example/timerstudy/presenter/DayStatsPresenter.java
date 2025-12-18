package com.example.timerstudy.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.example.timerstudy.data.remote.ApiService;
import com.example.timerstudy.data.repository.SessionRepository;
import com.example.timerstudy.data.repository.StatisticsCacheApiRepository;
import com.example.timerstudy.data.repository.StatisticsRepository;
import com.example.timerstudy.data.repository.StatisticsRepository.DailyStats;
import com.example.timerstudy.utils.UserManager;
import com.example.timerstudy.view.contracts.DayStatsContract;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DayStatsPresenter implements DayStatsContract.Presenter {

    private static final String TAG = "DayStatsPresenter";
    
    private final StatisticsCacheApiRepository apiRepository;
    private final StatisticsRepository localRepository; // Keep for timeline sessions
    private final SessionRepository sessionRepository;
    private DayStatsContract.View view;
    private final long userId;
    private final Handler mainHandler;
    private final Context context;

    public DayStatsPresenter(Context context, long userId) {
        this.context = context.getApplicationContext();
        this.apiRepository = StatisticsCacheApiRepository.getInstance();
        this.apiRepository.setContext(this.context);
        this.localRepository = StatisticsRepository.getInstance(this.context);
        this.sessionRepository = SessionRepository.getInstance(this.context);
        this.userId = userId;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void attach(DayStatsContract.View view) {
        this.view = view;
    }

    @Override
    public void detach() {
        view = null;
        // Remove any pending callbacks
        mainHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void loadDay(Date start, Date end) {
        runOnMainThread(() -> {
            if (view != null) view.showLoading(true);
        });

        // Convert Date to Unix timestamp (seconds as double)
        double dateTimestamp = start.getTime() / 1000.0;
        
        Log.d(TAG, "Loading daily statistics for date: " + dateTimestamp);
        
        // Call API to get daily statistics
        apiRepository.getDailyStatistics(dateTimestamp, new StatisticsCacheApiRepository.StatisticsCacheCallback<ApiService.DailyStatisticsResponse>() {
            @Override
            public void onSuccess(ApiService.DailyStatisticsResponse apiResponse) {
                Log.d(TAG, "API Success - Sessions: " + apiResponse.totalSessions + ", Focus: " + apiResponse.totalFocusTime);
                
                // Convert API response to DailyStats format
                DailyStats stats = new DailyStats(
                    apiResponse.totalFocusTime, // totalFocusMinutes
                    apiResponse.totalSessions   // completedSessions
                );
                
                // Fetch sessions from API first, then get timeline from local DB
                fetchSessionsAndShowTimeline(stats, start, end);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "API Error: " + errorMessage);
                runOnMainThread(() -> {
                    if (view != null) {
                        view.showLoading(false);
                        view.showError("Không thể tải thống kê: " + errorMessage);
                    }
                });
            }
        });
    }
    
    /**
     * Load timeline with retry mechanism
     */
    private void loadTimelineWithRetry(DailyStats stats, Date start, Date end, int retryCount) {
        final int MAX_RETRIES = 5;
        final long DELAY_MS = 1000; // 1 second delay
        
        if (retryCount >= MAX_RETRIES) {
            Log.e(TAG, "Max retries reached, loading from local DB anyway");
            loadTimelineFromLocalDB(stats, start, end);
            return;
        }
        
        mainHandler.postDelayed(() -> {
            localRepository.loadDailyStats(userId, start, end, new StatisticsRepository.DailyStatsCallback() {
                @Override
                public void onSuccess(DailyStats localStats, List<SessionEntity> timeline) {
                    Log.d(TAG, "Timeline loaded (attempt " + (retryCount + 1) + "): " + timeline.size() + " sessions");
                    
                    // If we have sessions or this is the last retry, show the result
                    if (timeline.size() > 0 || retryCount >= MAX_RETRIES - 1) {
                        runOnMainThread(() -> {
                            if (view != null) {
                                view.showLoading(false);
                                view.showDailyStats(stats); // Use API stats
                                view.showTimeline(timeline); // Use local timeline
                            }
                        });
                    } else {
                        // Retry if no sessions found
                        Log.d(TAG, "No sessions found, retrying... (attempt " + (retryCount + 1) + "/" + MAX_RETRIES + ")");
                        loadTimelineWithRetry(stats, start, end, retryCount + 1);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Timeline load error (attempt " + (retryCount + 1) + "): " + errorMessage);
                    if (retryCount >= MAX_RETRIES - 1) {
                        // Last retry, show empty timeline
                        runOnMainThread(() -> {
                            if (view != null) {
                                view.showLoading(false);
                                view.showDailyStats(stats);
                                view.showTimeline(new ArrayList<>());
                            }
                        });
                    } else {
                        // Retry
                        loadTimelineWithRetry(stats, start, end, retryCount + 1);
                    }
                }
            });
        }, DELAY_MS);
    }
    
    /**
     * Load timeline directly from local DB (fallback)
     */
    private void loadTimelineFromLocalDB(DailyStats stats, Date start, Date end) {
        localRepository.loadDailyStats(userId, start, end, new StatisticsRepository.DailyStatsCallback() {
            @Override
            public void onSuccess(DailyStats localStats, List<SessionEntity> timeline) {
                runOnMainThread(() -> {
                    if (view != null) {
                        view.showLoading(false);
                        view.showDailyStats(stats);
                        view.showTimeline(timeline);
                        Log.d(TAG, "Timeline loaded from local DB: " + timeline.size() + " sessions");
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnMainThread(() -> {
                    if (view != null) {
                        view.showLoading(false);
                        view.showDailyStats(stats);
                        view.showTimeline(new ArrayList<>());
                        Log.d(TAG, "Timeline load failed: " + errorMessage);
                    }
                });
            }
        });
    }
    
    /**
     * Fetch sessions from API and then show timeline from local DB
     */
    private void fetchSessionsAndShowTimeline(DailyStats stats, Date start, Date end) {
        try {
            UserManager userManager = UserManager.getInstance(context);
            com.example.timerstudy.model.User user = userManager.getCurrentUser();
            
            if (user != null && user.getAccessToken() != null && !user.getAccessToken().isEmpty()) {
                String accessToken = user.getAccessToken();
                Log.d(TAG, "Fetching sessions from API for timeline...");
                
                // Fetch all sessions from API and save to local DB
                sessionRepository.fetchAndSaveSessionsFromApi(accessToken, userId);
                
                // Wait longer for sessions to be saved, then load from local DB
                // Use retry mechanism to ensure sessions are loaded
                loadTimelineWithRetry(stats, start, end, 0);
            } else {
                // No token, just load from local DB
                Log.d(TAG, "No access token, loading from local DB only");
                loadTimelineFromLocalDB(stats, start, end);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching sessions", e);
            // Fallback to local DB
            loadTimelineFromLocalDB(stats, start, end);
        }
    }

    private void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // Already on main thread
            runnable.run();
        } else {
            // Post to main thread
            mainHandler.post(runnable);
        }
    }
}