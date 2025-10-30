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
        private final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
        VH(ItemTimelineSessionBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }
        void bind(SessionEntity s) {
            b.textTitle.setText(String.valueOf(s.getSessionType()));
            String start = s.getStartTime() != null ? timeFmt.format(s.getStartTime()) : "--:--";
            String end = s.getEndTime() != null ? timeFmt.format(s.getEndTime()) : "--:--";
            b.textTimeRange.setText(start + " - " + end);
        }
    }

    private static final DiffUtil.ItemCallback<SessionEntity> DIFF = new DiffUtil.ItemCallback<SessionEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull SessionEntity oldItem, @NonNull SessionEntity newItem) {
            return oldItem.getSessionId() == newItem.getSessionId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull SessionEntity oldItem, @NonNull SessionEntity newItem) {
            return oldItem.equals(newItem);
        }
    };
}


