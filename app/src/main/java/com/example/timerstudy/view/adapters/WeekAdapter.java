package com.example.timerstudy.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timerstudy.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.DayViewHolder> {

    private final List<Date> days;
    private final Locale en = Locale.ENGLISH;
    private final SimpleDateFormat dayOfWeekFmt = new SimpleDateFormat("EEE", en); // Mon, Tue, ...
    private final SimpleDateFormat dayOfMonthFmt = new SimpleDateFormat("dd/MM", en);

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
        String dayName = dayOfWeekFmt.format(d); // Mon, Tue, ...
        holder.tvDayOfWeek.setText(dayName);
        holder.tvDate.setText(dayOfMonthFmt.format(d));
        // highlight today
        Calendar a = Calendar.getInstance();
        a.setTime(d);
        Calendar today = Calendar.getInstance();
        boolean isToday = a.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && a.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                && a.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
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
