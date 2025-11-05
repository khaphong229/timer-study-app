package com.example.timerstudy.view.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.example.timerstudy.databinding.ItemTimelineSessionBinding;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TimelineAdapter extends ListAdapter<SessionEntity, TimelineAdapter.VH> {

    public TimelineAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTimelineSessionBinding b = ItemTimelineSessionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position));
    }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemTimelineSessionBinding b;
        private final SimpleDateFormat dateFmt = new SimpleDateFormat("EEE, dd/MM/yyyy", Locale.getDefault());
        VH(ItemTimelineSessionBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }
        void bind(SessionEntity s) {
            // Title: ngày tạo (sessionDate)
            String dateStr = s.getSessionDate() != null ? dateFmt.format(s.getSessionDate()) : "--";
            b.textTitle.setText(dateStr);

            // Subtitle: thời gian học (actualDurationMinutes or durationMinutes)
            Integer actual = s.getActualDurationMinutes();
            int minutes = actual != null ? actual : s.getDurationMinutes();
            b.textTimeRange.setText(formatMinutes(minutes));
        }

        private String formatMinutes(int minutes) {
            int h = minutes / 60;
            int m = minutes % 60;
            if (h > 0) {
                return h + "h " + String.format(Locale.getDefault(), "%02dm", m);
            } else {
                return m + "m";
            }
        }
    }

    private static final DiffUtil.ItemCallback<SessionEntity> DIFF = new DiffUtil.ItemCallback<SessionEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull SessionEntity oldItem, @NonNull SessionEntity newItem) {
            return oldItem.getSessionId() == newItem.getSessionId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull SessionEntity oldItem, @NonNull SessionEntity newItem) {
            // Compare meaningful fields to determine content equality
            if (oldItem.getSessionId() != newItem.getSessionId()) return false;
            if (oldItem.getDurationMinutes() != newItem.getDurationMinutes()) return false;
            if (oldItem.isCompleted() != newItem.isCompleted()) return false;
            if (oldItem.getFocusSessionCount() != newItem.getFocusSessionCount()) return false;
            if (oldItem.getPauseCount() != newItem.getPauseCount()) return false;
            if (oldItem.getTotalPauseDuration() != newItem.getTotalPauseDuration()) return false;

            String oldType = oldItem.getSessionType();
            String newType = newItem.getSessionType();
            if (oldType != null ? !oldType.equals(newType) : newType != null) return false;

            String oldStatus = oldItem.getStatus();
            String newStatus = newItem.getStatus();
            if (oldStatus != null ? !oldStatus.equals(newStatus) : newStatus != null) return false;

            java.util.Date oldStart = oldItem.getStartTime();
            java.util.Date newStart = newItem.getStartTime();
            if (oldStart != null ? !oldStart.equals(newStart) : newStart != null) return false;

            java.util.Date oldEnd = oldItem.getEndTime();
            java.util.Date newEnd = newItem.getEndTime();
            if (oldEnd != null ? !oldEnd.equals(newEnd) : newEnd != null) return false;

            return true;
        }
    };
}


