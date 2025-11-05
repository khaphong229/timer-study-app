package com.example.timerstudy.contract;

import com.example.timerstudy.model.SoundItem;
import java.util.List;

public interface SoundContract {
    interface View {
        void updateSoundList(List<SoundItem> sounds);

        void showVolumeSlider(String soundName, float volume);

        void hideVolumeSlider();

        void updatePlayingState(String soundName, boolean isPlaying);
    }

    interface Presenter {
        void restorePlayingState();

        void onSoundItemClicked(String soundName);

        void updateVolumeOnly(String soundName, float volume);

        void setRepeatMode(boolean repeatMode);

        boolean isRepeatMode();

        void onDestroy();

        SoundItem findSoundByName(String name);

        List<SoundItem> getSoundItems();

        void playNextSound(String currentSoundName);
    }
}
