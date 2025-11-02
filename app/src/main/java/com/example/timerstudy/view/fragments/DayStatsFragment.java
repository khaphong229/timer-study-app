package com.example.timerstudy.view.fragments;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.timerstudy.databinding.FragmentDayStatsBinding;
import com.example.timerstudy.view.adapters.TimelineAdapter;
import com.example.timerstudy.view.contracts.DayStatsContract;
import com.example.timerstudy.presenter.DayStatsPresenter;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DayStatsFragment extends Fragment implements DayStatsContract.View {

    private FragmentDayStatsBinding binding;
    private DayStatsPresenter presenter;
    private Calendar currentDate = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDayStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupPresenter();
        setupRecyclerView();
        setupSwipeRefresh();
        setupDateNavigation();
        loadTodayStats();
    }

    private void setupPresenter() {
        presenter = new DayStatsPresenter(requireContext(), 1);
        presenter.attach(this);
    }

    private void setupRecyclerView() {
        binding.recyclerTimeline.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerTimeline.setHasFixedSize(true);
        TimelineAdapter adapter = new TimelineAdapter();
        binding.recyclerTimeline.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        if (binding.swipeRefresh != null) {
            binding.swipeRefresh.setOnRefreshListener(() -> loadDayStats(currentDate));
        }
    }

    private void setupDateNavigation() {
        if (binding.btnPreviousDay != null) {
            binding.btnPreviousDay.setOnClickListener(v -> navigateDay(-1));
        }
        if (binding.btnNextDay != null) {
            binding.btnNextDay.setOnClickListener(v -> navigateDay(1));
        }
        if (binding.btnToday != null) {
            binding.btnToday.setOnClickListener(v -> loadTodayStats());
        }
    }

    private void navigateDay(int offset) {
        currentDate.add(Calendar.DAY_OF_MONTH, offset);
        loadDayStats(currentDate);
        updateDateDisplay();
    }

    private void loadTodayStats() {
        currentDate = Calendar.getInstance();
        loadDayStats(currentDate);
        updateDateDisplay();
    }

    private void loadDayStats(Calendar date) {
        Calendar calStart = (Calendar) date.clone();
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        Calendar calEnd = (Calendar) calStart.clone();
        calEnd.add(Calendar.DAY_OF_MONTH, 1);

        presenter.loadDay(calStart.getTime(), calEnd.getTime());
    }

    private void updateDateDisplay() {
        if (binding.textCurrentDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
            binding.textCurrentDate.setText(sdf.format(currentDate.getTime()));
        }
    }

    private String formatMinutes(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return h + "h " + String.format(Locale.getDefault(), "%02dm", m);
    }

    @Override
    public void showDailyStats(com.example.timerstudy.data.repository.StatisticsRepository.DailyStats stats) {
        // Đảm bảo chạy trên UI thread
        if (!isAdded()) return;

        requireActivity().runOnUiThread(() -> {
            binding.textTotalTime.setText(formatMinutes(stats.totalFocusMinutes));
            binding.textSessions.setText(String.valueOf(stats.completedSessions));

            // Update progress indicators if available
            if (binding.progressDaily != null) {
                int dailyGoal = 240; // 4 hours default goal
                int progress = Math.min(100, (stats.totalFocusMinutes * 100) / dailyGoal);
                animateProgress(progress);
            }

            // Show motivational message
            if (binding.textMotivation != null) {
                binding.textMotivation.setText(getMotivationalMessage(stats.totalFocusMinutes, stats.completedSessions));
            }
        });
    }

    @Override
    public void showTimeline(java.util.List<com.example.timerstudy.data.local.database.entities.SessionEntity> sessions) {
        if (!isAdded()) return;

        requireActivity().runOnUiThread(() -> {
            TimelineAdapter adapter = (TimelineAdapter) binding.recyclerTimeline.getAdapter();
            if (adapter != null) {
                adapter.submitList(sessions);
            }

            // Show empty state if no sessions
            if (binding.layoutEmpty != null) {
                binding.layoutEmpty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
            }
            binding.recyclerTimeline.setVisibility(sessions.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void showLoading(boolean loading) {
        if (!isAdded()) return;

        requireActivity().runOnUiThread(() -> {
            if (binding.swipeRefresh != null) {
                binding.swipeRefresh.setRefreshing(loading);
            }

            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            }

            // Disable interaction during loading
            if (binding.contentLayout != null) {
                binding.contentLayout.setAlpha(loading ? 0.5f : 1.0f);
            }
        });
    }

    @Override
    public void showError(String message) {
        if (!isAdded() || getView() == null) return;

        requireActivity().runOnUiThread(() -> {
            Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                    .setAction("Thử lại", v -> loadTodayStats());

//            if (binding.fabStartSession != null) {
//                snackbar.setAnchorView(binding.fabStartSession);
//            }

            snackbar.show();
        });
    }

    private void animateProgress(int targetProgress) {
        if (binding.progressDaily == null || !isAdded()) return;

        ValueAnimator animator = ValueAnimator.ofInt(0, targetProgress);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            if (isAdded() && binding != null && binding.progressDaily != null) {
                binding.progressDaily.setProgress((int) animation.getAnimatedValue());
            }
        });
        animator.start();
    }

    private String getMotivationalMessage(int minutes, int sessions) {
        if (minutes >= 240) return "🔥 Xuất sắc! Bạn đã hoàn thành mục tiêu!";
        if (minutes >= 120) return "💪 Làm tốt lắm! Tiếp tục phát huy!";
        if (sessions > 0) return "👍 Khởi đầu tốt đấy!";
        return "🚀 Hãy bắt đầu phiên học đầu tiên!";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detach();
        }
        binding = null;
    }
}