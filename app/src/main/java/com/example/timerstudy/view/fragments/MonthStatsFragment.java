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

import com.example.timerstudy.databinding.FragmentMonthStatsBinding;
import com.example.timerstudy.view.adapters.TimelineAdapter;
import com.example.timerstudy.view.contracts.MonthStatsContract;
import com.example.timerstudy.presenter.MonthStatsPresenter;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MonthStatsFragment extends Fragment implements MonthStatsContract.View {

    private FragmentMonthStatsBinding binding;
    private MonthStatsPresenter presenter;
    private Calendar currentMonth = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMonthStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupPresenter();
        setupRecyclerView();
        setupSwipeRefresh();
        setupMonthNavigation();
        loadThisMonth();
    }

    private void setupPresenter() {
        presenter = new MonthStatsPresenter(requireContext(), 1);
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
            binding.swipeRefresh.setOnRefreshListener(this::reloadCurrentMonth);
        }
    }

    private void setupMonthNavigation() {
        if (binding.btnPreviousMonth != null) {
            binding.btnPreviousMonth.setOnClickListener(v -> navigateMonth(-1));
        }
        if (binding.btnNextMonth != null) {
            binding.btnNextMonth.setOnClickListener(v -> navigateMonth(1));
        }
        if (binding.btnThisMonth != null) {
            binding.btnThisMonth.setOnClickListener(v -> loadThisMonth());
        }
    }

    private void navigateMonth(int offset) {
        currentMonth.add(Calendar.MONTH, offset);
        reloadCurrentMonth();
        updateMonthDisplay();
    }

    private void loadThisMonth() {
        currentMonth = Calendar.getInstance();
        reloadCurrentMonth();
        updateMonthDisplay();
    }

    private void reloadCurrentMonth() {
        Calendar start = (Calendar) currentMonth.clone();
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);
        presenter.loadMonth(start.getTime(), end.getTime());
    }

    private void updateMonthDisplay() {
        if (binding.textCurrentMonth != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM, yyyy", Locale.getDefault());
            binding.textCurrentMonth.setText(sdf.format(currentMonth.getTime()));
        }
    }

    private String formatMinutes(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return h + "h " + String.format(Locale.getDefault(), "%02dm", m);
    }

    @Override
    public void showMonthlyStats(com.example.timerstudy.data.repository.StatisticsRepository.MonthlyStats stats) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            binding.textTotalTime.setText(formatMinutes(stats.totalFocusMinutes));
            int monthlyGoal = 60 * 60; // example: 60 hours
            int progress = monthlyGoal > 0 ? Math.min(100, (stats.totalFocusMinutes * 100) / monthlyGoal) : 0;
            animateProgress(progress);
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
            if (binding.textSessions != null) {
                int completed = 0;
                for (com.example.timerstudy.data.local.database.entities.SessionEntity s : sessions) {
                    if (s.isCompleted()) completed++;
                }
                binding.textSessions.setText(String.valueOf(completed));
            }
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
                    .setAction("Thử lại", v -> reloadCurrentMonth());
            snackbar.show();
        });
    }

    private void animateProgress(int targetProgress) {
        if (binding.progressMonthly == null || !isAdded()) return;
        ValueAnimator animator = ValueAnimator.ofInt(0, targetProgress);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            if (isAdded() && binding != null && binding.progressMonthly != null) {
                binding.progressMonthly.setProgress((int) animation.getAnimatedValue());
            }
        });
        animator.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) presenter.detach();
        binding = null;
    }
}
