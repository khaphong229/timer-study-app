package com.example.timerstudy.view.fragments;

import android.animation.ValueAnimator;
import android.graphics.Color;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
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
        if (!isAdded())
            return;
        requireActivity().runOnUiThread(() -> {
            binding.textTotalTime.setText(formatMinutes(stats.totalFocusMinutes));
            int monthlyGoal = 60 * 60; // example: 60 hours
            int progress = monthlyGoal > 0 ? Math.min(100, (stats.totalFocusMinutes * 100) / monthlyGoal) : 0;
            animateProgress(progress);
        });
    }

    @Override
    public void showTimeline(
            java.util.List<com.example.timerstudy.data.local.database.entities.SessionEntity> sessions) {
        if (!isAdded())
            return;
        requireActivity().runOnUiThread(() -> {
            // Aggregate minutes per day in the month
            java.text.SimpleDateFormat dayKey = new java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault());
            java.util.Map<String, Integer> dayToMinutes = new java.util.LinkedHashMap<>();
            for (com.example.timerstudy.data.local.database.entities.SessionEntity s : sessions) {
                if (s.getSessionDate() == null)
                    continue;
                String key = dayKey.format(s.getSessionDate());
                Integer actual = s.getActualDurationMinutes();
                int minutes = (actual != null ? actual : s.getDurationMinutes());
                dayToMinutes.put(key, dayToMinutes.getOrDefault(key, 0) + minutes);
            }

            java.util.ArrayList<java.util.Map.Entry<String, Integer>> items = new java.util.ArrayList<>(
                    dayToMinutes.entrySet());
            items.sort((a, b) -> {
                try {
                    int da = Integer.parseInt(a.getKey().substring(0, 2));
                    int db = Integer.parseInt(b.getKey().substring(0, 2));
                    return Integer.compare(da, db);
                } catch (Exception ex) {
                    return a.getKey().compareTo(b.getKey());
                }
            });

            java.util.ArrayList<com.github.mikephil.charting.data.Entry> entries = new java.util.ArrayList<>();
            java.util.ArrayList<String> labels = new java.util.ArrayList<>();
            for (int idx = 0; idx < items.size(); idx++) {
                java.util.Map.Entry<String, Integer> e = items.get(idx);
                entries.add(new com.github.mikephil.charting.data.Entry(idx, e.getValue()));
                labels.add(e.getKey());
            }

            com.github.mikephil.charting.data.LineDataSet dataSet = new com.github.mikephil.charting.data.LineDataSet(
                    entries, "Sessions");

            // FIX: Thay thế dòng bị lỗi bằng màu cụ thể (dòng 162)
            int color;
            try {
                color = requireContext().getColor(com.example.timerstudy.R.color.chart_primary);
            } catch (Exception e) {
                // Fallback to hardcoded color if resource not found
                color = Color.parseColor("#2196F3");
            }

            dataSet.setColor(color);
            dataSet.setCircleColor(color);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(3f);
            dataSet.setMode(com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawValues(true);
            com.github.mikephil.charting.data.LineData data = new com.github.mikephil.charting.data.LineData(dataSet);
            data.setValueTextSize(9f);

            if (binding.chartTimelineMonth != null) {
                binding.chartTimelineMonth.getDescription().setEnabled(true);
                binding.chartTimelineMonth.getDescription().setText("Day");
                binding.chartTimelineMonth.getLegend().setEnabled(true);
                binding.chartTimelineMonth.setScaleEnabled(false);
                binding.chartTimelineMonth.getAxisRight().setEnabled(false);
                com.github.mikephil.charting.components.XAxis xAxis = binding.chartTimelineMonth.getXAxis();
                xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);
                xAxis.setDrawGridLines(false);
                xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));
                com.github.mikephil.charting.components.YAxis yAxis = binding.chartTimelineMonth.getAxisLeft();
                yAxis.setAxisMinimum(0f);
                yAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return Math.max(0, Math.round(value)) + " min";
                    }
                });

                binding.chartTimelineMonth.setData(data);
                binding.chartTimelineMonth.invalidate();
                binding.chartTimelineMonth.setVisibility(entries.isEmpty() ? View.GONE : View.VISIBLE);
            }

            if (binding.textSessions != null) {
                int completed = 0;
                for (com.example.timerstudy.data.local.database.entities.SessionEntity s : sessions) {
                    if (s.isCompleted())
                        completed++;
                }
                binding.textSessions.setText(String.valueOf(completed));
            }
            if (binding.layoutEmpty != null) {
                boolean empty = sessions.isEmpty();
                binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
            // Keep RecyclerView hidden when using chart
            binding.recyclerTimeline.setVisibility(View.GONE);
        });
    }

    @Override
    public void showLoading(boolean loading) {
        if (!isAdded())
            return;
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
        if (!isAdded() || getView() == null)
            return;
        requireActivity().runOnUiThread(() -> {
            Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                    .setAction("Thử lại", v -> reloadCurrentMonth());
            snackbar.show();
        });
    }

    private void animateProgress(int targetProgress) {
        if (binding.progressMonthly == null || !isAdded())
            return;
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
        if (presenter != null)
            presenter.detach();
        binding = null;
    }
}
