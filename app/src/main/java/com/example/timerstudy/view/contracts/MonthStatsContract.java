package com.example.timerstudy.view.contracts;

import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.example.timerstudy.data.repository.StatisticsRepository.MonthlyStats;

import java.util.Date;
import java.util.List;

public interface MonthStatsContract {
    interface View {
        void showMonthlyStats(MonthlyStats stats);
        void showTimeline(List<SessionEntity> sessions);
        void showLoading(boolean loading);
        void showError(String message);
    }

    interface Presenter {
        void attach(View view);
        void detach();
        void loadMonth(Date start, Date end);
    }
}
