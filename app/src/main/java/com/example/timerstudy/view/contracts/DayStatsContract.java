package com.example.timerstudy.view.contracts;

import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.example.timerstudy.data.repository.StatisticsRepository.DailyStats;

import java.util.Date;
import java.util.List;

public interface DayStatsContract {
    interface View {
        void showDailyStats(DailyStats stats);
        void showTimeline(List<SessionEntity> sessions);
        void showLoading(boolean loading);
        void showError(String message);
    }

    interface Presenter {
        void attach(View view);
      void detach();
       void loadDay(Date start, Date end);
    }
}