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

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.DayViewHolder> {

    private final List<Date> days;
    private final Locale en = Locale.ENGLISH;
    private final SimpleDateFormat dayOfWeekFmt = new SimpleDateFormat("EEEE", en); // Monday, Tuesday...
    private final SimpleDateFormat dayOfMonthFmt = new SimpleDateFormat("dd/MM", en);
    private Date selectedDate;
    private OnDayClickListener onDayClickListener;

    public interface OnDayClickListener {
        void onDayClick(Date date, int position);
    }

    public MonthAdapter(List<Date> days, OnDayClickListener listener) {
        this.days = days;
        this.onDayClickListener = listener;
        // Default select today
        this.selectedDate = new Date();
    }

    public void setSelectedDate(Date date) {
        this.selectedDate = date;
        notifyDataSetChanged();
    }

    public Date getSelectedDate() {
        return selectedDate;
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
        
        // Day of week name (Monday, Tuesday...)
        String dayName = dayOfWeekFmt.format(d);
        if (dayName != null && dayName.length() > 0) {
            dayName = dayName.substring(0, 1).toUpperCase(en) + dayName.substring(1);
        }
        holder.tvDayOfWeek.setText(dayName);
        holder.tvDate.setText(dayOfMonthFmt.format(d));

        // Highlight ngày hôm nay
        java.util.Calendar dayCalendar = java.util.Calendar.getInstance();
        dayCalendar.setTime(d);
        java.util.Calendar today = java.util.Calendar.getInstance();
        boolean isToday = dayCalendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR)
                && dayCalendar.get(java.util.Calendar.MONTH) == today.get(java.util.Calendar.MONTH)
                && dayCalendar.get(java.util.Calendar.DAY_OF_MONTH) == today.get(java.util.Calendar.DAY_OF_MONTH);

        // Highlight ngày được chọn
        boolean isSelected = false;
        if (selectedDate != null) {
            java.util.Calendar selectedCalendar = java.util.Calendar.getInstance();
            selectedCalendar.setTime(selectedDate);
            isSelected = dayCalendar.get(java.util.Calendar.YEAR) == selectedCalendar.get(java.util.Calendar.YEAR)
                    && dayCalendar.get(java.util.Calendar.MONTH) == selectedCalendar.get(java.util.Calendar.MONTH)
                    && dayCalendar.get(java.util.Calendar.DAY_OF_MONTH) == selectedCalendar.get(java.util.Calendar.DAY_OF_MONTH);
        }

        if (isToday) {
            holder.itemView.setBackgroundResource(R.drawable.bg_day_today);
        } else if (isSelected) {
            holder.itemView.setBackgroundResource(R.drawable.bg_day_selected);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_day_selector);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onDayClickListener != null) {
                setSelectedDate(d);
                onDayClickListener.onDayClick(d, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days == null ? 0 : days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayOfWeek, tvDate;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}