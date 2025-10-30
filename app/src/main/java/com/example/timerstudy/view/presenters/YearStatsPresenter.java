package com.example.timerstudy.view.presenters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.timerstudy.data.repository.StatisticsRepository;
import com.example.timerstudy.data.repository.StatisticsRepository.YearlyStats;
import com.example.timerstudy.view.contracts.YearStatsContract;

import java.util.Date;

public class YearStatsPresenter implements YearStatsContract.Presenter {

    private final StatisticsRepository repository;
    private YearStatsContract.View view;
    private final int userId;
    private final Handler mainHandler;

    public YearStatsPresenter(Context context, int userId) {
        this.repository = StatisticsRepository.getInstance(context.getApplicationContext());
        this.userId = userId;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void attach(YearStatsContract.View view) {
        this.view = view;
    }

    @Override
    public void detach() {
        view = null;
        mainHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void loadYear(Date start, Date end) {
        runOnMainThread(() -> {
            if (view != null) view.showLoading(true);
        });

        repository.loadYearlyStats(userId, start, end, new StatisticsRepository.YearlyStatsCallback() {
            @Override
            public void onSuccess(YearlyStats stats) {
                runOnMainThread(() -> {
                    if (view != null) {
                        view.showLoading(false);
                        view.showYearlyStats(stats);
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