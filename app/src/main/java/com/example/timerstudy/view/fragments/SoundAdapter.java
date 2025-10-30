package com.example.timerstudy.view.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.timerstudy.R;
import com.example.timerstudy.model.SoundItem;

import java.util.ArrayList;
import java.util.List;

public class SoundAdapter extends RecyclerView.Adapter<SoundAdapter.SoundViewHolder> {
    private List<SoundItem> soundItems;
    private OnSoundClickListener listener;

    public interface OnSoundClickListener {
        void onSoundClick(String soundName);
    }

    public SoundAdapter(OnSoundClickListener listener) {
        this.soundItems = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public SoundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sound, parent, false);
        return new SoundViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SoundViewHolder holder, int position) {
        SoundItem item = soundItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return soundItems.size();
    }

    public void updateSounds(List<SoundItem> sounds) {
        this.soundItems.clear();
        this.soundItems.addAll(sounds);
        notifyDataSetChanged();
    }

    public void updatePlayingState(String soundName, boolean isPlaying) {
        for (int i = 0; i < soundItems.size(); i++) {
            if (soundItems.get(i).getName().equals(soundName)) {
                soundItems.get(i).setPlaying(isPlaying);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public List<SoundItem> getSoundItems() {
        return soundItems;
    }

    class SoundViewHolder extends RecyclerView.ViewHolder {
        private TextView soundName;
        private View itemView;

        public SoundViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            soundName = itemView.findViewById(R.id.soundName);
        }

        public void bind(SoundItem item) {
            soundName.setText(item.getName());
            
            // Change appearance based on playing state
            if (item.isPlaying()) {
                soundName.setTextColor(itemView.getContext().getResources().getColor(R.color.blue_accent, null));
                itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.light_blue_bg, null));
            } else {
                soundName.setTextColor(itemView.getContext().getResources().getColor(R.color.gray_text, null));
                itemView.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.transparent, null));
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSoundClick(item.getName());
                }
            });
        }
    }
}
