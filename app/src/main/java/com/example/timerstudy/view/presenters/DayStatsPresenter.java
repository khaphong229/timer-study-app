package com.example.timerstudy.view.presenters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.example.timerstudy.data.repository.StatisticsRepository;
import com.example.timerstudy.data.repository.StatisticsRepository.DailyStats;
import com.example.timerstudy.view.contracts.DayStatsContract;

import java.util.Date;
import java.util.List;

public class DayStatsPresenter implements DayStatsContract.Presenter {

    private final StatisticsRepository repository;
    private DayStatsContract.View view;
    private final int userId;
    private final Handler mainHandler;

    public DayStatsPresenter(Context context, int userId) {
        this.repository = StatisticsRepository.getInstance(context.getApplicationContext());
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

        repository.loadDailyStats(userId, start, end, new StatisticsRepository.DailyStatsCallback() {
            @Override
            public void onSuccess(DailyStats stats, List<SessionEntity> timeline) {
                runOnMainThread(() -> {
                    if (view != null) {
                        view.showLoading(false);
                        view.showDailyStats(stats);
                        view.showTimeline(timeline);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnMainThread(() -> {
                    if (view != null) {
                        view.showLoading(false);
                        view.showError(errorMessage);
                    }
                });
            }
        });
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