package com.example.timerstudy.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import com.example.timerstudy.R;
import com.example.timerstudy.model.SoundItem;
import com.example.timerstudy.service.AudioPlayerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundPresenter {
    private SoundView view;
    private List<SoundItem> soundItems;
    private Map<String, MediaPlayer> mediaPlayers;
    private Context context;

    private static final String PREF_NAME = "audio_pref";
    private static final String KEY_PLAYING = "playing_audio";

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
        // Xoá dòng này:
        // restorePlayingState();
    }

    private void initializeSounds() {
        soundItems.add(new SoundItem("City streets", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("A Jazz Piano", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("Holiday", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("Super Spiffy", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("Peaceful horizons", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("Miss you", R.raw.test, false, 0.5f));
        
        soundItems.add(new SoundItem("Library", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("Night", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("Rain", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("Train", R.raw.test, false, 0.5f));
        soundItems.add(new SoundItem("Storm", R.raw.test, false, 0.5f));
        
        view.updateSoundList(soundItems);
    }

    public void restorePlayingState() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String playingName = prefs.getString(KEY_PLAYING, null);
        if (playingName != null) {
            SoundItem item = findSoundByName(playingName);
            if (item != null) {
                item.setPlaying(true);
                if (view != null) {
                    view.updatePlayingState(item.getName(), true);
                    view.showVolumeSlider(item.getName(), item.getVolume());
                }
            }
        }
    }

    private void savePlayingState(String soundName) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PLAYING, soundName).apply();
    }

    private void clearPlayingState() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_PLAYING).apply();
    }

    public void onSoundItemClicked(String soundName) {
        SoundItem item = findSoundByName(soundName);
        if (item != null) {
            if (item.isPlaying()) {
                stopSound(soundName);
            } else {
                stopAllSounds();
                playSound(soundName, item);
            }
        }
    }

    private void playSound(String soundName, SoundItem item) {
        Intent intent = new Intent(context, AudioPlayerService.class);
        intent.setAction(AudioPlayerService.ACTION_PLAY);
        intent.putExtra(AudioPlayerService.EXTRA_RES_ID, item.getResourceId());
        intent.putExtra(AudioPlayerService.EXTRA_VOLUME, item.getVolume());
        context.startService(intent);

        item.setPlaying(true);
        savePlayingState(soundName);
        view.updatePlayingState(soundName, true);
        view.showVolumeSlider(soundName, item.getVolume());
    }

    private void stopSound(String soundName) {
        Intent intent = new Intent(context, AudioPlayerService.class);
        intent.setAction(AudioPlayerService.ACTION_STOP);
        context.startService(intent);

        SoundItem item = findSoundByName(soundName);
        if (item != null) {
            item.setPlaying(false);
            clearPlayingState();
            view.updatePlayingState(soundName, false);
        }
    }

    private void stopAllSounds() {
        Intent intent = new Intent(context, AudioPlayerService.class);
        intent.setAction(AudioPlayerService.ACTION_STOP);
        context.startService(intent);

        for (SoundItem item : soundItems) {
            if (item.isPlaying()) {
                item.setPlaying(false);
                view.updatePlayingState(item.getName(), false);
            }
        }
        clearPlayingState();
    }

    public void onVolumeChanged(String soundName, float volume) {
        SoundItem item = findSoundByName(soundName);
        if (item != null) {
            item.setVolume(volume);
            if (item.isPlaying()) {
                Intent intent = new Intent(context, AudioPlayerService.class);
                intent.setAction(AudioPlayerService.ACTION_PLAY);
                intent.putExtra(AudioPlayerService.EXTRA_RES_ID, item.getResourceId());
                intent.putExtra(AudioPlayerService.EXTRA_VOLUME, volume);
                context.startService(intent);
            }
        }
    }

    public void onDestroy() {
        Intent intent = new Intent(context, AudioPlayerService.class);
        intent.setAction(AudioPlayerService.ACTION_STOP);
        context.startService(intent);
    }

    public SoundItem findSoundByName(String name) {
        for (SoundItem item : soundItems) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }
}
