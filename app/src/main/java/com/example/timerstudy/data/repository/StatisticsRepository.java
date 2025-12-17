package com.example.timerstudy.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timerstudy.data.local.database.AppDatabase;
import com.example.timerstudy.data.local.database.dao.SessionDao;
import com.example.timerstudy.data.local.database.dao.StatisticsDao;
import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.example.timerstudy.data.local.database.entities.StatisticsCacheEntity;
import com.example.timerstudy.data.local.database.entities.StreakRecordEntity;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for aggregated statistics: daily/monthly/yearly, streaks, comparisons.
 */
public class StatisticsRepository {

    private static final String TAG = "StatisticsRepository";

    private final AppDatabase database;
    private final SessionDao sessionDao;
    private final StatisticsDao statisticsDao;
    private final ExecutorService executorService;

    // LiveData outputs for UI
    private final MutableLiveData<DailyStats> dailyStatsLiveData;
    private final MutableLiveData<MonthlyStats> monthlyStatsLiveData;
    private final MutableLiveData<YearlyStats> yearlyStatsLiveData;
    private final MutableLiveData<StreakStats> streakStatsLiveData;
    private final MutableLiveData<PeriodComparison> comparisonLiveData;
    private final MutableLiveData<List<SessionEntity>> dayTimelineLiveData;
    private final MutableLiveData<Boolean> isLoadingLiveData;
    private final MutableLiveData<String> errorLiveData;

    private static volatile StatisticsRepository INSTANCE;

    private StatisticsRepository(Context context) {
        database = AppDatabase.getDatabase(context);
        sessionDao = database.sessionDao();
        statisticsDao = database.statisticsDao();
        executorService = Executors.newFixedThreadPool(4);

        dailyStatsLiveData = new MutableLiveData<>();
        monthlyStatsLiveData = new MutableLiveData<>();
        yearlyStatsLiveData = new MutableLiveData<>();
        streakStatsLiveData = new MutableLiveData<>();
        comparisonLiveData = new MutableLiveData<>();
        dayTimelineLiveData = new MutableLiveData<>();
        isLoadingLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
    }

    public static StatisticsRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (StatisticsRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new StatisticsRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<DailyStats> getDailyStatsLiveData() { return dailyStatsLiveData; }
    public LiveData<MonthlyStats> getMonthlyStatsLiveData() { return monthlyStatsLiveData; }
    public LiveData<YearlyStats> getYearlyStatsLiveData() { return yearlyStatsLiveData; }
    public LiveData<StreakStats> getStreakStatsLiveData() { return streakStatsLiveData; }
    public LiveData<PeriodComparison> getComparisonLiveData() { return comparisonLiveData; }
    public LiveData<List<SessionEntity>> getDayTimelineLiveData() { return dayTimelineLiveData; }
    public LiveData<Boolean> getIsLoadingLiveData() { return isLoadingLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }

    // ==================== Day / Month / Year ====================

    // CỦA MÔ HÌNH MVVP KHÔNG DÙNG DO ĐÃ ĐỔI SANG MVP NHA KÊNH CHAT
    public void loadDailyStats(long userId, Date dayStart, Date dayEnd) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                long start = dayStart.getTime();
                long end = dayEnd.getTime();
                int totalFocusMin = sessionDao.getTotalFocusTimeByUserAndDateRange(userId, start, end);
                int completedSessions = sessionDao.getCompletedSessionCountByUser(userId); // global
                List<SessionEntity> timeline = sessionDao.getTodaySessions(userId, start, end);

                DailyStats stats = new DailyStats(totalFocusMin, completedSessions);
                dailyStatsLiveData.postValue(stats);
                dayTimelineLiveData.postValue(timeline);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading daily stats", e);
                errorLiveData.postValue("Failed to load daily stats: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public interface DailyStatsCallback {
        void onSuccess(DailyStats stats, List<SessionEntity> timeline);
        void onError(String errorMessage);
    }

    public void loadDailyStats(long userId, Date dayStart, Date dayEnd, DailyStatsCallback callback) {
        executorService.execute(() -> {
            try {
                long start = dayStart.getTime();
                long end = dayEnd.getTime();
                int totalFocusMin = sessionDao.getTotalFocusTimeByUserAndDateRange(userId, start, end);
                int completedSessions = sessionDao.getCompletedSessionCountByUserAndDateRange(userId, start, end);
                List<SessionEntity> timeline = sessionDao.getTodaySessions(userId, start, end);
                Log.d(TAG, "loadDailyStats: " + timeline.size());
                Log.d(TAG, "loadDailyStats: " + timeline);
                Gson gson = new Gson();
                Log.d(TAG, "loadDailyStats: " + gson.toJson(timeline));

                DailyStats stats = new DailyStats(totalFocusMin, completedSessions);
                callback.onSuccess(stats, timeline);
            } catch (Exception e) {
                callback.onError("Failed to load daily stats: " + e.getMessage());
            }
        });
    }

    public void loadMonthlyStats(long userId, Date monthStart, Date monthEnd) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                long start = monthStart.getTime();
                long end = monthEnd.getTime();
                int totalFocusMin = sessionDao.getTotalFocusTimeByUserAndDateRange(userId, start, end);

                // Days with activity in month: derive from sessions
                List<SessionEntity> sessions = sessionDao.getSessionsByUserAndDateRange(userId, start, end);
                int activeDays = DateUtils.countDistinctDays(sessions);
                int avgPerDay = activeDays > 0 ? totalFocusMin / activeDays : 0;

                MonthlyStats stats = new MonthlyStats(totalFocusMin, activeDays, avgPerDay);
                monthlyStatsLiveData.postValue(stats);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading monthly stats", e);
                errorLiveData.postValue("Failed to load monthly stats: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public interface MonthlyStatsCallback {
        void onSuccess(MonthlyStats stats);
        void onError(String errorMessage);
    }

    public interface MonthlyStatsWithTimelineCallback {
        void onSuccess(MonthlyStats stats, List<SessionEntity> timeline);
        void onError(String errorMessage);
    }

    public void loadMonthlyStats(long userId, Date monthStart, Date monthEnd, MonthlyStatsCallback callback) {
        executorService.execute(() -> {
            try {
                long start = monthStart.getTime();
                long end = monthEnd.getTime();
                int totalFocusMin = sessionDao.getTotalFocusTimeByUserAndDateRange(userId, start, end);
                List<SessionEntity> sessions = sessionDao.getSessionsByUserAndDateRange(userId, start, end);
                int activeDays = DateUtils.countDistinctDays(sessions);
                int avgPerDay = activeDays > 0 ? totalFocusMin / activeDays : 0;
                MonthlyStats stats = new MonthlyStats(totalFocusMin, activeDays, avgPerDay);
                callback.onSuccess(stats);
            } catch (Exception e) {
                callback.onError("Failed to load monthly stats: " + e.getMessage());
            }
        });
    }

    public void loadMonthlyStats(long userId, Date monthStart, Date monthEnd, MonthlyStatsWithTimelineCallback callback) {
        executorService.execute(() -> {
            try {
                long start = monthStart.getTime();
                long end = monthEnd.getTime();
                int totalFocusMin = sessionDao.getTotalFocusTimeByUserAndDateRange(userId, start, end);
                List<SessionEntity> sessions = sessionDao.getSessionsByUserAndDateRange(userId, start, end);
                int activeDays = DateUtils.countDistinctDays(sessions);
                int avgPerDay = activeDays > 0 ? totalFocusMin / activeDays : 0;
                MonthlyStats stats = new MonthlyStats(totalFocusMin, activeDays, avgPerDay);
                callback.onSuccess(stats, sessions);
            } catch (Exception e) {
                callback.onError("Failed to load monthly stats: " + e.getMessage());
            }
        });
    }

    public void loadYearlyStats(long userId, Date yearStart, Date yearEnd) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                long start = yearStart.getTime();
                long end = yearEnd.getTime();
                int totalFocusMin = sessionDao.getTotalFocusTimeByUserAndDateRange(userId, start, end);

                YearlyStats stats = new YearlyStats(totalFocusMin);
                yearlyStatsLiveData.postValue(stats);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading yearly stats", e);
                errorLiveData.postValue("Failed to load yearly stats: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public interface YearlyStatsCallback {
        void onSuccess(YearlyStats stats);
        void onError(String errorMessage);
    }

    public void loadYearlyStats(long userId, Date yearStart, Date yearEnd, YearlyStatsCallback callback) {
        executorService.execute(() -> {
            try {
                long start = yearStart.getTime();
                long end = yearEnd.getTime();
                int totalFocusMin = sessionDao.getTotalFocusTimeByUserAndDateRange(userId, start, end);
                YearlyStats stats = new YearlyStats(totalFocusMin);
                callback.onSuccess(stats);
            } catch (Exception e) {
                callback.onError("Failed to load yearly stats: " + e.getMessage());
            }
        });
    }

    // ==================== Streaks ====================

    public void loadStreakStats(long userId, Date rangeStart, Date rangeEnd) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                // Simple computation from sessions list (fallback if no dedicated queries)
                List<SessionEntity> sessions = sessionDao.getSessionsByUserAndDateRange(userId, rangeStart.getTime(), rangeEnd.getTime());
                StreakStats streaks = DateUtils.computeStreaks(sessions, rangeStart, rangeEnd);
                streakStatsLiveData.postValue(streaks);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error loading streak stats", e);
                errorLiveData.postValue("Failed to load streak stats: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    // ==================== Comparisons ====================

    public void compareMonths(long userId, Date thisMonthStart, Date thisMonthEnd, Date prevMonthStart, Date prevMonthEnd) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                int thisTotal = sessionDao.getTotalFocusTimeByUserAndDateRange(userId, thisMonthStart.getTime(), thisMonthEnd.getTime());
                int prevTotal = sessionDao.getTotalFocusTimeByUserAndDateRange(userId, prevMonthStart.getTime(), prevMonthEnd.getTime());
                PeriodComparison cmp = PeriodComparison.of(thisTotal, prevTotal);
                comparisonLiveData.postValue(cmp);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error comparing months", e);
                errorLiveData.postValue("Failed to compare months: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void compareYears(long userId, Date thisYearStart, Date thisYearEnd, Date prevYearStart, Date prevYearEnd) {
        executorService.execute(() -> {
            try {
                isLoadingLiveData.postValue(true);
                int thisTotal = sessionDao.getTotalFocusTimeByUserAndDateRange(userId, thisYearStart.getTime(), thisYearEnd.getTime());
                int prevTotal = sessionDao.getTotalFocusTimeByUserAndDateRange(userId, prevYearStart.getTime(), prevYearEnd.getTime());
                PeriodComparison cmp = PeriodComparison.of(thisTotal, prevTotal);
                comparisonLiveData.postValue(cmp);
                errorLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error comparing years", e);
                errorLiveData.postValue("Failed to compare years: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void close() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    public boolean isClosed() {
        return executorService == null || executorService.isShutdown();
    }

    // ==================== DTOs ====================

    public static class DailyStats {
        public final int totalFocusMinutes;
        public final int completedSessions;
        public DailyStats(int totalFocusMinutes, int completedSessions) {
            this.totalFocusMinutes = totalFocusMinutes;
            this.completedSessions = completedSessions;
        }
    }

    public static class MonthlyStats {
        public final int totalFocusMinutes;
        public final int activeDays;
        public final int averagePerActiveDayMinutes;
        public MonthlyStats(int totalFocusMinutes, int activeDays, int averagePerActiveDayMinutes) {
            this.totalFocusMinutes = totalFocusMinutes;
            this.activeDays = activeDays;
            this.averagePerActiveDayMinutes = averagePerActiveDayMinutes;
        }
    }

    public static class YearlyStats {
        public final int totalFocusMinutes;
        public YearlyStats(int totalFocusMinutes) {
            this.totalFocusMinutes = totalFocusMinutes;
        }
    }

    public static class StreakStats {
        public final int currentStreak;
        public final int bestStreak;
        public StreakStats(int currentStreak, int bestStreak) {
            this.currentStreak = currentStreak;
            this.bestStreak = bestStreak;
        }
    }

    public static class PeriodComparison {
        public final int currentTotal;
        public final int previousTotal;
        public final double percentageChange; // positive for increase, negative for decrease

        private PeriodComparison(int currentTotal, int previousTotal, double percentageChange) {
            this.currentTotal = currentTotal;
            this.previousTotal = previousTotal;
            this.percentageChange = percentageChange;
        }

        public static PeriodComparison of(int currentTotal, int previousTotal) {
            double pct;
            if (previousTotal == 0) {
                pct = currentTotal > 0 ? 100.0 : 0.0;
            } else {
                pct = ((double) (currentTotal - previousTotal) / (double) previousTotal) * 100.0;
            }
            return new PeriodComparison(currentTotal, previousTotal, pct);
        }
    }

    // ==================== Utilities ====================
    private static class DateUtils {
        static int countDistinctDays(List<SessionEntity> sessions) {
            java.util.HashSet<String> days = new java.util.HashSet<>();
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
            for (SessionEntity s : sessions) {
                if (s.getSessionDate() != null) {
                    days.add(fmt.format(s.getSessionDate()));
                }
            }
            return days.size();
        }

        static StreakStats computeStreaks(List<SessionEntity> sessions, Date start, Date end) {
            java.util.HashSet<String> activeDaySet = new java.util.HashSet<>();
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
            for (SessionEntity s : sessions) {
                if (s.getSessionDate() != null) {
                    activeDaySet.add(fmt.format(s.getSessionDate()));
                }
            }

            Calendar cursor = Calendar.getInstance();
            cursor.setTime(end);
            // Normalize to day
            cursor.set(Calendar.HOUR_OF_DAY, 0);
            cursor.set(Calendar.MINUTE, 0);
            cursor.set(Calendar.SECOND, 0);
            cursor.set(Calendar.MILLISECOND, 0);

            Calendar startCal = Calendar.getInstance();
            startCal.setTime(start);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);

            int current = 0;
            int best = 0;
            while (!cursor.before(startCal)) {
                String key = fmt.format(cursor.getTime());
                if (activeDaySet.contains(key)) {
                    current++;
                    if (current > best) best = current;
                } else {
                    // break streak when missing a day
                    if (!cursor.equals(Calendar.getInstance())) {
                        current = 0;
                    }
                }
                cursor.add(Calendar.DAY_OF_MONTH, -1);
            }
            return new StreakStats(current, best);
        }
    }
}


