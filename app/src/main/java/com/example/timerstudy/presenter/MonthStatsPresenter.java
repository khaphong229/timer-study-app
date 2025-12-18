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
import com.example.timerstudy.data.repository.StatisticsRepository.MonthlyStats;
import com.example.timerstudy.utils.UserManager;
import com.example.timerstudy.view.contracts.MonthStatsContract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MonthStatsPresenter implements MonthStatsContract.Presenter {

    private static final String TAG = "MonthStatsPresenter";
    
    private final StatisticsCacheApiRepository apiRepository;
    private final StatisticsRepository localRepository; // Keep for timeline sessions
    private final SessionRepository sessionRepository;
    private MonthStatsContract.View view;
    private final long userId;
    private final Handler mainHandler;
    private final Context context;

    public MonthStatsPresenter(Context context, long userId) {
        this.context = context.getApplicationContext();
        this.apiRepository = StatisticsCacheApiRepository.getInstance();
        this.apiRepository.setContext(this.context);
        this.localRepository = StatisticsRepository.getInstance(this.context);
        this.sessionRepository = SessionRepository.getInstance(this.context);
        this.userId = userId;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void attach(MonthStatsContract.View view) {
        this.view = view;
    }

    @Override
    public void detach() {
        view = null;
        mainHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void loadMonth(Date start, Date end) {
        runOnMainThread(() -> {
            if (view != null) view.showLoading(true);
        });

        // Extract year and month from start date
        Calendar cal = Calendar.getInstance();
        cal.setTime(start);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based, API expects 1-based
        
        Log.d(TAG, "Loading monthly statistics for year: " + year + ", month: " + month);
        
        // Call API to get monthly statistics
        apiRepository.getMonthlyStatistics(year, month, new StatisticsCacheApiRepository.StatisticsCacheCallback<ApiService.MonthlyStatisticsResponse>() {
            @Override
            public void onSuccess(ApiService.MonthlyStatisticsResponse apiResponse) {
                Log.d(TAG, "API Success - Sessions: " + apiResponse.totalSessions + ", Focus: " + apiResponse.totalFocusTime);
                
                // Calculate active days and average (API doesn't provide these, so we estimate)
                // For now, use totalFocusTime and estimate active days
                int estimatedActiveDays = apiResponse.totalSessions > 0 ? 
                    Math.max(1, apiResponse.totalSessions / 3) : 0; // Rough estimate
                int avgPerDay = estimatedActiveDays > 0 ? apiResponse.totalFocusTime / estimatedActiveDays : 0;
                
                // Convert API response to MonthlyStats format
                MonthlyStats stats = new MonthlyStats(
                    apiResponse.totalFocusTime,  // totalFocusMinutes
                    estimatedActiveDays,         // activeDays (estimated)
                    avgPerDay                   // averagePerActiveDayMinutes
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
     * Fetch sessions from API and then show timeline from local DB
     */
    private void fetchSessionsAndShowTimeline(MonthlyStats stats, Date start, Date end) {
        try {
            UserManager userManager = UserManager.getInstance(context);
            com.example.timerstudy.model.User user = userManager.getCurrentUser();
            
            if (user != null && user.getAccessToken() != null && !user.getAccessToken().isEmpty()) {
                String accessToken = user.getAccessToken();
                Log.d(TAG, "Fetching sessions from API for timeline...");
                
                // Fetch all sessions from API and save to local DB
                sessionRepository.fetchAndSaveSessionsFromApi(accessToken, userId);
                
                // Wait a bit for sessions to be saved, then load from local DB
                mainHandler.postDelayed(() -> {
                    localRepository.loadMonthlyStats(userId, start, end, new StatisticsRepository.MonthlyStatsWithTimelineCallback() {
                        @Override
                        public void onSuccess(MonthlyStats localStats, List<SessionEntity> timeline) {
                            runOnMainThread(() -> {
                                if (view != null) {
                                    view.showLoading(false);
                                    view.showMonthlyStats(stats); // Use API stats
                                    view.showTimeline(timeline); // Use local timeline
                                    Log.d(TAG, "Timeline loaded: " + timeline.size() + " sessions");
                                }
                            });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            // Even if local fails, show API stats with empty timeline
                            runOnMainThread(() -> {
                                if (view != null) {
                                    view.showLoading(false);
                                    view.showMonthlyStats(stats);
                                    view.showTimeline(new ArrayList<>());
                                    Log.d(TAG, "Timeline load failed, showing empty timeline");
                                }
                            });
                        }
                    });
                }, 500); // Wait 500ms for API fetch to complete
            } else {
                // No token, just load from local DB
                Log.d(TAG, "No access token, loading from local DB only");
                localRepository.loadMonthlyStats(userId, start, end, new StatisticsRepository.MonthlyStatsWithTimelineCallback() {
                    @Override
                    public void onSuccess(MonthlyStats localStats, List<SessionEntity> timeline) {
                        runOnMainThread(() -> {
                            if (view != null) {
                                view.showLoading(false);
                                view.showMonthlyStats(stats);
                                view.showTimeline(timeline);
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnMainThread(() -> {
                            if (view != null) {
                                view.showLoading(false);
                                view.showMonthlyStats(stats);
                                view.showTimeline(new ArrayList<>());
                            }
                        });
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching sessions", e);
            // Fallback to local DB
            localRepository.loadMonthlyStats(userId, start, end, new StatisticsRepository.MonthlyStatsWithTimelineCallback() {
                @Override
                public void onSuccess(MonthlyStats localStats, List<SessionEntity> timeline) {
                    runOnMainThread(() -> {
                        if (view != null) {
                            view.showLoading(false);
                            view.showMonthlyStats(stats);
                            view.showTimeline(timeline);
                        }
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnMainThread(() -> {
                        if (view != null) {
                            view.showLoading(false);
                            view.showMonthlyStats(stats);
                            view.showTimeline(new ArrayList<>());
                        }
                    });
                }
            });
        }
    }

    private void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }
}