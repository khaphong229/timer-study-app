package com.example.timerstudy.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.cardview.widget.CardView;

import com.example.timerstudy.R;
import com.example.timerstudy.data.remote.ApiService;
import com.example.timerstudy.presenter.LeaderboardPresenter;
import com.example.timerstudy.utils.UserManager;
import com.example.timerstudy.view.adapters.LeaderboardAdapter;
import com.example.timerstudy.view.contracts.LeaderboardContract;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class LeaderboardFragment extends Fragment implements LeaderboardContract.View {

    private static final String TAG = "LeaderboardFragment";

    private TextView tabToday, tabWeek, tabAllTime;
    private CardView cvAvatar1, cvAvatar2, cvAvatar3;
    private TextView tvName1, tvScore1, tvName2, tvScore2, tvName3, tvScore3;
    private RecyclerView rvLeaderboard;
    private View listContainer;
    private LeaderboardAdapter adapter;
    private TextView tvMyRank, tvMyName, tvMyLevel, tvMyScore;

    private View loadingOverlay;

    private LeaderboardPresenter presenter;
    private String currentMetric = "focus_time";
    private String currentPeriod = "all_time";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initPresenter();
        setupRecycler();
        setupTabs();

        UserManager.getInstance(requireContext()).debugUserState();
        presenter.loadLeaderboard("all_time", currentMetric);
    }

    private void initViews(View view) {
        tabToday = view.findViewById(R.id.tabToday);
        tabWeek = view.findViewById(R.id.tabWeek);
        tabAllTime = view.findViewById(R.id.tabAllTime);

        cvAvatar1 = view.findViewById(R.id.cvAvatar1);
        cvAvatar2 = view.findViewById(R.id.cvAvatar2);
        cvAvatar3 = view.findViewById(R.id.cvAvatar3);

        tvName1 = view.findViewById(R.id.tvName1);
        tvScore1 = view.findViewById(R.id.tvScore1);
        tvName2 = view.findViewById(R.id.tvName2);
        tvScore2 = view.findViewById(R.id.tvScore2);
        tvName3 = view.findViewById(R.id.tvName3);
        tvScore3 = view.findViewById(R.id.tvScore3);

        rvLeaderboard = view.findViewById(R.id.rvLeaderboard);
        listContainer = view.findViewById(R.id.layoutList);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        tvMyRank = view.findViewById(R.id.tvMyRank);
        tvMyName = view.findViewById(R.id.tvMyName);
        tvMyLevel = view.findViewById(R.id.tvMyLevel);
        tvMyScore = view.findViewById(R.id.tvMyScore);
    }

    private void initPresenter() {
        presenter = new LeaderboardPresenter(this, UserManager.getInstance(requireContext()));
    }

    private void setupRecycler() {
        adapter = new LeaderboardAdapter();
        adapter.setMetric(currentMetric);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvLeaderboard.setAdapter(adapter);
    }

    private void setupTabs() {
        tabToday.setOnClickListener(v -> {
            selectTab("daily");
            presenter.onTabSelected("daily");
        });
        tabWeek.setOnClickListener(v -> {
            selectTab("weekly");
            presenter.onTabSelected("weekly");
        });
        tabAllTime.setOnClickListener(v -> {
            selectTab("all_time");
            presenter.onTabSelected("all_time");
        });
    }

    private void selectTab(String period) {
        this.currentPeriod = period;
        resetTabs();
        switch (period) {
            case "daily":
                tabToday.setBackgroundResource(R.drawable.bg_tab_selected);
                tabToday.setTextColor(0xFFFFFFFF);
                break;
            case "weekly":
                tabWeek.setBackgroundResource(R.drawable.bg_tab_selected);
                tabWeek.setTextColor(0xFFFFFFFF);
                break;
            default:
                tabAllTime.setBackgroundResource(R.drawable.bg_tab_selected);
                tabAllTime.setTextColor(0xFFFFFFFF);
                break;
        }
    }

    private void resetTabs() {
        tabToday.setBackground(null);
        tabWeek.setBackground(null);
        tabAllTime.setBackground(null);
        int inactiveColor = 0xFF757575;
        tabToday.setTextColor(inactiveColor);
        tabWeek.setTextColor(inactiveColor);
        tabAllTime.setTextColor(inactiveColor);
    }

    @Override
    public void showLoading() {
        if (loadingOverlay != null && isAdded()) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLoading() {
        if (loadingOverlay != null && isAdded()) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }

    @Override
    public void showLeaderboard(ApiService.LeaderboardData data) {
        if (!isAdded())
            return;
        android.util.Log.d(TAG,
                "showLeaderboard called with " + (data.entries != null ? data.entries.size() : 0) + " entries");
    }

    @Override
    public void showError(String message) {
        if (!isAdded())
            return;
        android.util.Log.e(TAG, "Showing error: " + message);
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void updatePodium(List<ApiService.LeaderboardEntry> topThree) {
        if (!isAdded())
            return;
        fillPodiumSlot(0, topThree.size() > 0 ? topThree.get(0) : null);
        fillPodiumSlot(1, topThree.size() > 1 ? topThree.get(1) : null);
        fillPodiumSlot(2, topThree.size() > 2 ? topThree.get(2) : null);
    }

    private void fillPodiumSlot(int index, ApiService.LeaderboardEntry entry) {
        if (!isAdded() || getContext() == null)
            return;

        CardView avatarView;
        TextView tvName, tvScore;
        switch (index) {
            case 0:
                avatarView = cvAvatar1;
                tvName = tvName1;
                tvScore = tvScore1;
                break;
            case 1:
                avatarView = cvAvatar2;
                tvName = tvName2;
                tvScore = tvScore2;
                break;
            default:
                avatarView = cvAvatar3;
                tvName = tvName3;
                tvScore = tvScore3;
                break;
        }

        if (avatarView == null || tvName == null || tvScore == null)
            return;

        ImageView iv = (ImageView) avatarView.getChildAt(0);
        if (iv == null)
            return;

        if (entry == null) {
            tvName.setText("—");
            tvScore.setText("—");
            iv.setImageResource(R.drawable.person_24dp);
            return;
        }

        tvName.setText(entry.displayName != null ? entry.displayName : "—");
        int score = getScoreByMetric(entry);
        tvScore.setText(formatScore(score));

        if (entry.profilePictureUrl != null && !entry.profilePictureUrl.isEmpty() && isAdded()
                && getContext() != null) {
            RequestOptions requestOptions = new RequestOptions()
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.person_24dp)
                    .error(R.drawable.person_24dp);

            try {
                Glide.with(this)
                        .load(entry.profilePictureUrl)
                        .apply(requestOptions)
                        .into(iv);
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error loading image with Glide", e);
                iv.setImageResource(R.drawable.person_24dp);
            }
        } else {
            iv.setImageResource(R.drawable.person_24dp);
        }
    }

    @Override
    public void updateList(List<ApiService.LeaderboardEntry> entries) {
        if (!isAdded())
            return;
        adapter.setMetric(currentMetric);
        adapter.setEntries(entries);
    }

    @Override
    public void updateCurrentUserRank(int rank, String name, int score) {
        if (!isAdded())
            return;
        tvMyRank.setText(String.valueOf(rank));
        tvMyName.setText(name != null ? name : "You");
        tvMyScore.setText(formatScore(score));
    }

    @Override
    public void showEmptyState() {
        if (!isAdded())
            return;
        adapter.setEntries(java.util.Collections.emptyList());
        Toast.makeText(requireContext(), "No entries", Toast.LENGTH_SHORT).show();
        updatePodium(java.util.Collections.emptyList());
        tvMyRank.setText("-");
        tvMyName.setText("You");
        tvMyScore.setText("-");
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

    private String formatScore(int score) {
        if (score >= 1_000_000)
            return String.format("%.1fM", score / 1_000_000.0);
        if (score >= 1_000)
            return String.format("%.1fK", score / 1_000.0);
        return String.valueOf(score);
    }

    @Override
    public void showLoginRequiredDialog() {
        if (!isAdded() || getContext() == null)
            return;
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Login Required")
                .setMessage("Please login with Facebook to view the leaderboard.")
                .setPositiveButton("Login", (dialog, which) -> {
                    try {
                        if (isAdded() && getView() != null) {
                            androidx.navigation.Navigation.findNavController(requireView())
                                    .navigate(R.id.profileFragment);
                        }
                    } catch (Exception e) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Please go to Profile to login", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    @Override
    public void showRetryDialog(String errorMessage) {
        if (!isAdded() || getContext() == null)
            return;
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Error Loading Leaderboard")
                .setMessage(errorMessage)
                .setPositiveButton("Retry", (dialog, which) -> {
                    if (isAdded()) {
                        presenter.loadLeaderboard(getCurrentPeriod(), currentMetric);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private String getCurrentPeriod() {
        return currentPeriod;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.onDestroy();
    }
}
