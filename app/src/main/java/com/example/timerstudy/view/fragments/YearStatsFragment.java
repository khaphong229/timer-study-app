package com.example.timerstudy.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.timerstudy.view.contracts.YearStatsContract;
import com.example.timerstudy.view.presenters.YearStatsPresenter;

import com.example.timerstudy.databinding.FragmentYearStatsBinding;

import java.util.Calendar;

public class YearStatsFragment extends Fragment implements YearStatsContract.View {

    private FragmentYearStatsBinding binding;
    private YearStatsPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentYearStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new YearStatsPresenter(requireContext(), 1);
        presenter.attach(this);

        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_YEAR, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.YEAR, 1);
        presenter.loadYear(start.getTime(), end.getTime());
    }

    private String formatMinutes(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return h + ":" + String.format("%02d", m);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detach();
        binding = null;
    }

    @Override
    public void showYearlyStats(com.example.timerstudy.data.repository.StatisticsRepository.YearlyStats stats) {
        binding.textYearTotal.setText(formatMinutes(stats.totalFocusMinutes));
    }

    @Override
    public void showLoading(boolean loading) {
        // no-op basic
    }

    @Override
    public void showError(String message) {
        // TODO
    }
}


