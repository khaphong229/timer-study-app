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
    private boolean isRepeatMode = false;

    private static final String PREF_NAME = "audio_pref";
    private static final String KEY_PLAYING = "playing_audio";
    private static final String KEY_REPEAT_MODE = "repeat_mode";

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
        soundItems.add(new SoundItem("Morning Time", R.raw.morning_time, false, 0.5f));
        soundItems.add(new SoundItem("After The Rain", R.raw.after_the_rain, false, 0.5f));
        soundItems.add(new SoundItem("Breathtaking", R.raw.breaktaking, false, 0.5f));
        soundItems.add(new SoundItem("Disappear", R.raw.disappear, false, 0.5f));
        soundItems.add(new SoundItem("Ethereal", R.raw.ethereal, false, 0.5f));
        soundItems.add(new SoundItem("Goodnight", R.raw.goodnight, false, 0.5f));
        soundItems.add(new SoundItem("Im Fine", R.raw.im_fine, false, 0.5f));
        soundItems.add(new SoundItem("Moonlight Drive", R.raw.moonlight_drive, false, 0.5f));
        soundItems.add(new SoundItem("No Words", R.raw.no_words, false, 0.5f));
        soundItems.add(new SoundItem("Viewfinder", R.raw.viewfinder, false, 0.5f));

        soundItems.add(new SoundItem("Am Tham Ben Em", R.raw.am_tham_ben_em, false, 0.5f));
        soundItems.add(new SoundItem("Buong Doi Tay Nhau Ra", R.raw.buong_doi_tay_nhau_ra, false, 0.5f));
        soundItems.add(new SoundItem("Chung Ta Cua Hien Tai", R.raw.chung_ta_cua_hien_tai, false, 0.5f));
        soundItems.add(new SoundItem("Chung Ta Khong Thuoc Ve Nhau", R.raw.chung_ta_khong_thuoc_ve_nhau, false, 0.5f));
        soundItems.add(new SoundItem("Lac Troi", R.raw.lac_troi, false, 0.5f));
        soundItems.add(new SoundItem("Noi Nay Co Anh", R.raw.noi_nay_co_anh, false, 0.5f));

        view.updateSoundList(soundItems);
    }

    public void restorePlayingState() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String playingName = prefs.getString(KEY_PLAYING, null);
        isRepeatMode = prefs.getBoolean(KEY_REPEAT_MODE, false);

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
        prefs.edit()
                .putString(KEY_PLAYING, soundName)
                .putBoolean(KEY_REPEAT_MODE, isRepeatMode)
                .apply();
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
        intent.putExtra(AudioPlayerService.EXTRA_REPEAT_MODE, isRepeatMode);
        intent.putExtra(AudioPlayerService.EXTRA_SOUND_NAME, soundName);
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
                // Chỉ cập nhật volume, không restart audio
                Intent intent = new Intent(context, AudioPlayerService.class);
                intent.setAction(AudioPlayerService.ACTION_SET_VOLUME);
                intent.putExtra(AudioPlayerService.EXTRA_VOLUME, volume);
                context.startService(intent);
            }
        }
    }

    public void updateVolumeOnly(String soundName, float volume) {
        SoundItem item = findSoundByName(soundName);
        if (item != null) {
            item.setVolume(volume);
            if (item.isPlaying()) {
                // Chỉ cập nhật volume
                Intent intent = new Intent(context, AudioPlayerService.class);
                intent.setAction(AudioPlayerService.ACTION_SET_VOLUME);
                intent.putExtra(AudioPlayerService.EXTRA_VOLUME, volume);
                context.startService(intent);
            }
        }
    }

    public void setRepeatMode(boolean repeatMode) {
        this.isRepeatMode = repeatMode;

        // Update service with new repeat mode
        Intent intent = new Intent(context, AudioPlayerService.class);
        intent.setAction(AudioPlayerService.ACTION_SET_REPEAT);
        intent.putExtra(AudioPlayerService.EXTRA_REPEAT_MODE, repeatMode);
        context.startService(intent);

        // Save repeat mode
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_REPEAT_MODE, repeatMode).apply();
    }

    public boolean isRepeatMode() {
        return isRepeatMode;
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

    // Thêm phương thức để lấy danh sách soundItems
    public List<SoundItem> getSoundItems() {
        return soundItems;
    }

    // Thêm phương thức playNextSound cho autoplay
    public void playNextSound(String currentSoundName) {
        if (soundItems.isEmpty())
            return;
        int currentIndex = -1;
        for (int i = 0; i < soundItems.size(); i++) {
            if (soundItems.get(i).getName().equals(currentSoundName)) {
                currentIndex = i;
                break;
            }
        }
        int nextIndex = (currentIndex + 1) % soundItems.size();
        SoundItem nextItem = soundItems.get(nextIndex);
        stopAllSounds();
        playSound(nextItem.getName(), nextItem);
    }
}
