package com.example.timerstudy.presenter;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import com.example.timerstudy.R;
import com.example.timerstudy.model.SoundItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundPresenter {
    private SoundView view;
    private List<SoundItem> soundItems;
    private Map<String, MediaPlayer> mediaPlayers;
    private Context context;

    public interface SoundView {
        void updateSoundList(List<SoundItem> sounds);
        void showVolumeSlider(String soundName, float volume);
        void hideVolumeSlider();
        void updatePlayingState(String soundName, boolean isPlaying);
    }

    public SoundPresenter(SoundView view, Context context) {
        this.view = view;
        this.context = context;
        this.soundItems = new ArrayList<>();
        this.mediaPlayers = new HashMap<>();
        initializeSounds();
    }

    private void initializeSounds() {
        // Sử dụng file test.mp3 từ thư mục res/raw/
        soundItems.add(new SoundItem("City streets", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("A Jazz Piano", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("Holiday", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("Super Spiffy", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("Peaceful horizons", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("Miss you", R.raw.test, false, 0.5f));
        
        // White noise sounds - sử dụng cùng file test.mp3
        soundItems.add(new SoundItem("Library", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("Night", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("Rain", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("Train", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("Storm", R.raw.test, false, 0.5f));
        
        view.updateSoundList(soundItems);
    }

    public void onSoundItemClicked(String soundName) {
        SoundItem item = findSoundByName(soundName);
        if (item != null) {
            if (item.isPlaying()) {
                stopSound(soundName);
            } else {
                playSound(soundName, item);
            }
        }
    }

    private void playSound(String soundName, SoundItem item) {
        try {
            // Sử dụng file test.mp3 từ resources
            MediaPlayer player = MediaPlayer.create(context, item.getResourceId());
            
            if (player != null) {
                player.setLooping(true);
                player.setVolume(item.getVolume(), item.getVolume());
                player.start();
                
                mediaPlayers.put(soundName, player);
                item.setPlaying(true);
                view.updatePlayingState(soundName, true);
                view.showVolumeSlider(soundName, item.getVolume());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopSound(String soundName) {
        MediaPlayer player = mediaPlayers.get(soundName);
        if (player != null) {
            player.stop();
            player.release();
            mediaPlayers.remove(soundName);
        }
        
        SoundItem item = findSoundByName(soundName);
        if (item != null) {
            item.setPlaying(false);
            view.updatePlayingState(soundName, false);
        }
    }

    public void onVolumeChanged(String soundName, float volume) {
        SoundItem item = findSoundByName(soundName);
        if (item != null) {
            item.setVolume(volume);
            MediaPlayer player = mediaPlayers.get(soundName);
            if (player != null) {
                player.setVolume(volume, volume);
            }
        }
    }

    public SoundItem findSoundByName(String name) {
        for (SoundItem item : soundItems) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    public void onDestroy() {
        for (MediaPlayer player : mediaPlayers.values()) {
            if (player != null) {
                player.stop();
                player.release();
            }
        }
        mediaPlayers.clear();
    }
}
