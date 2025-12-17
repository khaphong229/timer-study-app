package com.example.timerstudy.view.contracts;

import com.example.timerstudy.data.remote.ApiService;
import java.util.List;

public interface LeaderboardContract {
    interface View {
        void showLoading();

        void hideLoading();

        void showLeaderboard(ApiService.LeaderboardData data);

        void showError(String message);

        void updatePodium(List<ApiService.LeaderboardEntry> topThree);

        void updateList(List<ApiService.LeaderboardEntry> entries);

        void updateCurrentUserRank(int rank, String name, int score);

        void showEmptyState();
    }

    interface Presenter {
        void loadLeaderboard(String period, String metric);

        void onTabSelected(String period);

        void onDestroy();
    }
}
