package com.example.timerstudy.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.timerstudy.R;
import com.example.timerstudy.data.remote.ApiService;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<ApiService.LeaderboardEntry> entries = new ArrayList<>();
    private String currentMetric = "focus_time";

    public void setEntries(List<ApiService.LeaderboardEntry> entries) {
        this.entries = entries;
        notifyDataSetChanged();
    }

    public void setMetric(String metric) {
        this.currentMetric = metric;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApiService.LeaderboardEntry entry = entries.get(position);

        holder.tvRank.setText(String.valueOf(entry.rank));
        holder.tvUserName.setText(entry.displayName);

        int score = getScoreByMetric(entry);
        holder.tvPoints.setText(formatScore(score));

        if (entry.profilePictureUrl != null && !entry.profilePictureUrl.isEmpty()) {
            com.bumptech.glide.request.RequestOptions requestOptions = new com.bumptech.glide.request.RequestOptions()
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.person_24dp)
                    .error(R.drawable.person_24dp);

            try {
                Glide.with(holder.itemView.getContext())
                        .load(entry.profilePictureUrl)
                        .apply(requestOptions)
                        .into(holder.ivAvatar);
            } catch (Exception e) {
                // Fallback to default image if Glide fails
                holder.ivAvatar.setImageResource(R.drawable.person_24dp);
            }
        } else {
            holder.ivAvatar.setImageResource(R.drawable.person_24dp);
        }

        // current user
        if (entry.isCurrentUser) {
            holder.itemView.setBackgroundResource(R.drawable.bg_item_rank);
            holder.tvUserName.setTextColor(holder.itemView.getContext().getColor(android.R.color.white));
            holder.tvRank.setTextColor(holder.itemView.getContext().getColor(android.R.color.white));
            holder.tvPoints.setTextColor(holder.itemView.getContext().getColor(android.R.color.white));
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent);
            holder.tvUserName.setTextColor(holder.itemView.getContext().getColor(android.R.color.black));
            holder.tvRank.setTextColor(holder.itemView.getContext().getColor(android.R.color.black));
            holder.tvPoints.setTextColor(holder.itemView.getContext().getColor(R.color.purple_500));
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    private int getScoreByMetric(ApiService.LeaderboardEntry entry) {
        switch (currentMetric) {
            case "focus_time":
                return entry.focusTime;
            case "sessions":
                return entry.sessions;
            case "tasks":
                return entry.tasks;
            case "streak":
                return entry.currentStreak;
            case "best_streak":
                return entry.bestStreak;
            case "goals":
                return entry.goals;
            default:
                return entry.score;
        }
    }

    private String formatScore(int score) {
        if (score >= 1000000) {
            return String.format("%.1fM", score / 1000000.0);
        } else if (score >= 1000) {
            return String.format("%.1fK", score / 1000.0);
        }
        return String.valueOf(score);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank;
        ImageView ivAvatar;
        TextView tvUserName;
        TextView tvPoints;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvPoints = itemView.findViewById(R.id.tvPoints);
        }
    }
}
