package com.example.timerstudy.view.presenters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.example.timerstudy.data.repository.StatisticsRepository;
import com.example.timerstudy.data.repository.StatisticsRepository.MonthlyStats;
import com.example.timerstudy.view.contracts.MonthStatsContract;

import java.util.Date;
import java.util.List;

public class MonthStatsPresenter implements MonthStatsContract.Presenter {

    private final StatisticsRepository repository;
    private MonthStatsContract.View view;
    private final int userId;
    private final Handler mainHandler;

    public MonthStatsPresenter(Context context, int userId) {
        this.repository = StatisticsRepository.getInstance(context.getApplicationContext());
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

        repository.loadMonthlyStats(userId, start, end, new StatisticsRepository.MonthlyStatsWithTimelineCallback() {
            @Override
            public void onSuccess(MonthlyStats stats, List<SessionEntity> timeline) {
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
                        view.showError(errorMessage);
                    }
                });
            }
        });
    }

    private void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }
}