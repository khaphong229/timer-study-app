package com.example.timerstudy.presenter;

import android.util.Log;
import com.example.timerstudy.data.remote.ApiService;
import com.example.timerstudy.data.remote.RetrofitClient;
import com.example.timerstudy.model.User;
import com.example.timerstudy.utils.UserManager;
import com.example.timerstudy.view.contracts.LeaderboardContract;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardPresenter implements LeaderboardContract.Presenter {

    private static final String TAG = "LeaderboardPresenter";
    private final LeaderboardContract.View view;
    private final UserManager userManager;
    private final ApiService apiService;
    private String currentPeriod = "all_time";
    private String currentMetric = "focus_time";

    public LeaderboardPresenter(LeaderboardContract.View view, UserManager userManager) {
        this.view = view;
        this.userManager = userManager;
        this.apiService = RetrofitClient.getInstance().getApiService();
    }

    @Override
    public void loadLeaderboard(String period, String metric) {
        this.currentPeriod = period;
        this.currentMetric = metric;

        userManager.debugUserState();

        User currentUser = userManager.getCurrentUser();

        if (currentUser == null) {
            view.hideLoading();
            view.showError("User data not found. Please login again.");
            return;
        }

        Log.d(TAG, "User found: " + currentUser.getName());
        Log.d(TAG, "IsLoggedIn: " + currentUser.isLoggedIn());

        if (!currentUser.isLoggedIn()) {
            view.hideLoading();
            view.showError("Please login with Facebook to view leaderboard");
            return;
        }

        String accessToken = currentUser.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            view.hideLoading();
            view.showError("Authentication token missing. Please logout and login again.");
            return;
        }

        if (currentUser.isTokenExpired()) {
            view.showLoading();
            userManager.checkAndRefreshToken(new com.example.timerstudy.data.repository.UserRepository.SyncCallback() {
                @Override
                public void onSuccess(User user) {
                    loadLeaderboard(period, metric);
                }

                @Override
                public void onError(String message) {
                    view.hideLoading();
                    view.showError("Session expired. Please login again");
                }
            });
            return;
        }

        view.showLoading();

        String authHeader = "Bearer " + accessToken;

        Call<ApiService.ApiResponse<ApiService.LeaderboardData>> call = apiService
                .getFacebookFriendsLeaderboard(authHeader, period, metric, 50, true);

        call.enqueue(new Callback<ApiService.ApiResponse<ApiService.LeaderboardData>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.LeaderboardData>> call,
                    Response<ApiService.ApiResponse<ApiService.LeaderboardData>> response) {
                view.hideLoading();

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    ApiService.LeaderboardData data = response.body().data;

                    if (data.entries == null || data.entries.isEmpty()) {
                        view.showEmptyState();
                    } else {
                        view.showLeaderboard(data);
                        updatePodiumAndList(data);
                    }
                } else {
                    String errorMsg = "Failed to load leaderboard";
                    if (response.body() != null && response.body().message != null) {
                        errorMsg = response.body().message;
                    }
                    Log.e(TAG, "Error response: " + errorMsg);
                    view.showError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.LeaderboardData>> call, Throwable t) {
                view.hideLoading();
                Log.e(TAG, "Network error", t);
                view.showError("Network error: " + t.getMessage());
            }
        });
    }

    private void updatePodiumAndList(ApiService.LeaderboardData data) {
        List<ApiService.LeaderboardEntry> topThree = new ArrayList<>();
        List<ApiService.LeaderboardEntry> restOfList = new ArrayList<>();

        for (int i = 0; i < data.entries.size(); i++) {
            if (i < 3) {
                topThree.add(data.entries.get(i));
            } else {
                restOfList.add(data.entries.get(i));
            }
        }

        view.updatePodium(topThree);

        view.updateList(restOfList);

        ApiService.LeaderboardEntry currentUserEntry = findCurrentUserEntry(data.entries);
        if (currentUserEntry != null) {
            view.updateCurrentUserRank(
                    currentUserEntry.rank,
                    currentUserEntry.displayName,
                    getScoreByMetric(currentUserEntry));
        } else if (data.currentUserRank > 0) {
            view.updateCurrentUserRank(data.currentUserRank, "You", 0);
        }
    }

    private ApiService.LeaderboardEntry findCurrentUserEntry(List<ApiService.LeaderboardEntry> entries) {
        for (ApiService.LeaderboardEntry entry : entries) {
            if (entry.isCurrentUser) {
                return entry;
            }
        }
        return null;
    }

    private int getScoreByMetric(ApiService.LeaderboardEntry entry) {
        switch (currentMetric) {
            case "focus_time":
                return entry.focusTime;
            case "sessions":
                return entry.sessions;
            case "tasks":
                return entry.tasks;
            case "streak":
                return entry.currentStreak;
            case "best_streak":
                return entry.bestStreak;
            case "goals":
                return entry.goals;
            default:
                return entry.score;
        }
    }

    @Override
    public void onTabSelected(String period) {
        loadLeaderboard(period, currentMetric);
    }

    @Override
    public void onDestroy() {
    }
}
