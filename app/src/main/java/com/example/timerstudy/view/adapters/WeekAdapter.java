package com.example.timerstudy.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timerstudy.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.DayViewHolder> {

    private final List<Date> days;
    private final Locale vi = Locale.forLanguageTag("vi-VN");
    private final SimpleDateFormat dayOfWeekFmt = new SimpleDateFormat("EEEE", vi); // Thứ hai
    private final SimpleDateFormat dayOfMonthFmt = new SimpleDateFormat("dd/MM", vi);

    public WeekAdapter(List<Date> days){
        this.days = days;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
        return new DayViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        Date d = days.get(position);
        // capitalize first letter of weekday (Vietnamese)
        String dayName = dayOfWeekFmt.format(d);
        if (dayName != null && dayName.length() > 0) {
            dayName = dayName.substring(0,1).toUpperCase(vi) + dayName.substring(1);
        }
        holder.tvDayOfWeek.setText(dayName);
        holder.tvDate.setText(dayOfMonthFmt.format(d));
        // highlight today
        java.util.Calendar a = java.util.Calendar.getInstance();
        a.setTime(d);
        java.util.Calendar today = java.util.Calendar.getInstance();
        boolean isToday = a.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR)
                && a.get(java.util.Calendar.MONTH) == today.get(java.util.Calendar.MONTH)
                && a.get(java.util.Calendar.DAY_OF_MONTH) == today.get(java.util.Calendar.DAY_OF_MONTH);
        holder.itemView.setSelected(isToday);
    }

    @Override
    public int getItemCount() {
        return days == null ? 0 : days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder{
        TextView tvDayOfWeek, tvDate;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
