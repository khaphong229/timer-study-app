package com.example.timerstudy.view.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.timerstudy.view.fragments.DayStatsFragment;
import com.example.timerstudy.view.fragments.MonthStatsFragment;

public class StatisticsPagerAdapter extends FragmentStateAdapter {

    public StatisticsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new DayStatsFragment();
        return new MonthStatsFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}


