package com.example.timerstudy.view.contracts;

import com.example.timerstudy.data.repository.StatisticsRepository.YearlyStats;

import java.util.Date;

public interface YearStatsContract {
    interface View {
        void showYearlyStats(YearlyStats stats);
        void showLoading(boolean loading);
        void showError(String message);
    }

    interface Presenter {
        void attach(View view);
        void detach();
        void loadYear(Date start, Date end);
    }
}
