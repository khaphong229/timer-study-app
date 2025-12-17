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

        Log.d(TAG, "=== LOAD LEADERBOARD START ===");

        // Debug user state trước khi check
        userManager.debugUserState();

        User currentUser = userManager.getCurrentUser();

        Log.d(TAG, "Checking user login status...");

        // Check if user exists
        if (currentUser == null) {
            Log.e(TAG, "ERROR: currentUser is NULL");
            view.hideLoading();
            view.showError("User data not found. Please login again.");
            return;
        }

        Log.d(TAG, "User found: " + currentUser.getName());
        Log.d(TAG, "IsLoggedIn: " + currentUser.isLoggedIn());
        Log.d(TAG, "LoginProvider: " + currentUser.getLoginProvider());

        // Check if user is logged in
        if (!currentUser.isLoggedIn()) {
            Log.e(TAG, "ERROR: User not logged in. isLoggedIn() = false");
            view.hideLoading();
            view.showError("Please login with Facebook to view leaderboard");
            return;
        }

        Log.d(TAG, "User is logged in");

        // Check if access token exists
        String accessToken = currentUser.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Log.e(TAG, "ERROR: Access token is " + (accessToken == null ? "NULL" : "EMPTY"));
            view.hideLoading();
            view.showError("Authentication token missing. Please logout and login again.");
            return;
        }

        Log.d(TAG, "Access token exists: " + accessToken.substring(0, Math.min(20, accessToken.length())) + "...");

        // Check if token is expired
        if (currentUser.isTokenExpired()) {
            Log.e(TAG, "ERROR: Access token expired");
            view.hideLoading();
            view.showError("Session expired. Please login again");
            return;
        }

        Log.d(TAG, "Token is valid. Proceeding with API call...");

        view.showLoading();

        String authHeader = "Bearer " + accessToken;

        Log.d(TAG, "=== API CALL PARAMETERS ===");
        Log.d(TAG, "Period: " + period);
        Log.d(TAG, "Metric: " + metric);
        Log.d(TAG, "Auth Header: " + authHeader.substring(0, Math.min(50, authHeader.length())) + "...");
        Log.d(TAG, "===========================");

        Call<ApiService.ApiResponse<ApiService.LeaderboardData>> call = apiService
                .getFacebookFriendsLeaderboard(authHeader, period, metric, 50, true);

        call.enqueue(new Callback<ApiService.ApiResponse<ApiService.LeaderboardData>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.LeaderboardData>> call,
                    Response<ApiService.ApiResponse<ApiService.LeaderboardData>> response) {
                view.hideLoading();

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    ApiService.LeaderboardData data = response.body().data;

                    Log.d(TAG, "=== LEADERBOARD SUCCESS ===");
                    Log.d(TAG, "Total participants: " + data.totalParticipants);
                    Log.d(TAG, "Current user rank: " + data.currentUserRank);
                    Log.d(TAG, "Entries count: " + data.entries.size());

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

        // Split entries into top 3 and rest
        for (int i = 0; i < data.entries.size(); i++) {
            if (i < 3) {
                topThree.add(data.entries.get(i));
            } else {
                restOfList.add(data.entries.get(i));
            }
        }

        // Update podium (top 3)
        view.updatePodium(topThree);

        // Update list (rank 4+)
        view.updateList(restOfList);

        // Update current user rank
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
        // Cleanup if needed
    }
}
