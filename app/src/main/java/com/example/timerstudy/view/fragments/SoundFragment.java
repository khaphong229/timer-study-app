package com.example.timerstudy.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timerstudy.R;
import com.example.timerstudy.model.SoundItem;
import com.example.timerstudy.presenter.SoundPresenter;

import java.util.List;

public class SoundFragment extends Fragment implements SoundPresenter.SoundView {
    private RecyclerView musicRecyclerView;
    private RecyclerView whiteNoiseRecyclerView;
    private TextView asmrTab, musicTab;
    private SeekBar volumeSeekBar;
    private TextView volumeLabel;
    private View volumeContainer;
    
    private SoundPresenter presenter;
    private SoundAdapter musicAdapter;
    private SoundAdapter whiteNoiseAdapter;
    private String currentPlayingSoundName = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sound, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerViews();
        setupTabs();
        setupVolumeControl();
        
        // Fix: Truyền cả SoundView (this) và Context (getContext())
        presenter = new SoundPresenter(this, getContext());
    }

    private void initViews(View view) {
        musicRecyclerView = view.findViewById(R.id.musicRecyclerView);
        whiteNoiseRecyclerView = view.findViewById(R.id.whiteNoiseRecyclerView);
        asmrTab = view.findViewById(R.id.asmrTab);
        musicTab = view.findViewById(R.id.musicTab);
        volumeSeekBar = view.findViewById(R.id.volumeSeekBar);
        volumeLabel = view.findViewById(R.id.volumeLabel);
        volumeContainer = view.findViewById(R.id.volumeContainer);
    }

    private void setupRecyclerViews() {
        musicAdapter = new SoundAdapter(this::onSoundItemClick);
        musicRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        musicRecyclerView.setAdapter(musicAdapter);
        
        whiteNoiseAdapter = new SoundAdapter(this::onSoundItemClick);
        whiteNoiseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        whiteNoiseRecyclerView.setAdapter(whiteNoiseAdapter);
    }

    private void setupTabs() {
        asmrTab.setOnClickListener(v -> {
            showWhiteNoiseView();
            updateTabSelection(true);
        });
        
        musicTab.setOnClickListener(v -> {
            showMusicView();
            updateTabSelection(false);
        });
        
        // Default to music view
        showMusicView();
        updateTabSelection(false);
    }

    private void setupVolumeControl() {
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && currentPlayingSoundName != null) {
                    float volume = progress / 100.0f;
                    presenter.onVolumeChanged(currentPlayingSoundName, volume);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        volumeContainer.setVisibility(View.GONE);
    }

    private void showMusicView() {
        musicRecyclerView.setVisibility(View.VISIBLE);
        whiteNoiseRecyclerView.setVisibility(View.GONE);
    }

    private void showWhiteNoiseView() {
        musicRecyclerView.setVisibility(View.GONE);
        whiteNoiseRecyclerView.setVisibility(View.VISIBLE);
    }

    private void updateTabSelection(boolean isAsmrSelected) {
        if (isAsmrSelected) {
            asmrTab.setTextColor(getResources().getColor(R.color.blue_accent, null));
            musicTab.setTextColor(getResources().getColor(R.color.gray_text, null));
        } else {
            asmrTab.setTextColor(getResources().getColor(R.color.gray_text, null));
            musicTab.setTextColor(getResources().getColor(R.color.blue_accent, null));
        }
    }

    private void onSoundItemClick(String soundName) {
        presenter.onSoundItemClicked(soundName);
    }

    @Override
    public void updateSoundList(List<SoundItem> sounds) {
        // Separate music and white noise sounds
        List<SoundItem> musicSounds = sounds.subList(0, Math.min(6, sounds.size())); // First 6 items
        List<SoundItem> whiteNoiseSounds = sounds.size() > 6 ? 
            sounds.subList(6, sounds.size()) : 
            sounds.subList(0, 0); // Empty list if not enough items
        
        musicAdapter.updateSounds(musicSounds);
        whiteNoiseAdapter.updateSounds(whiteNoiseSounds);
    }

    @Override
    public void showVolumeSlider(String soundName, float volume) {
        currentPlayingSoundName = soundName;
        volumeLabel.setText("Volume");
        volumeSeekBar.setProgress((int)(volume * 100));
        volumeContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideVolumeSlider() {
        currentPlayingSoundName = null;
        volumeContainer.setVisibility(View.GONE);
    }

    @Override
    public void updatePlayingState(String soundName, boolean isPlaying) {
        musicAdapter.updatePlayingState(soundName, isPlaying);
        whiteNoiseAdapter.updatePlayingState(soundName, isPlaying);
        
        if (isPlaying) {
            // Find the sound item and show volume slider
            SoundItem item = presenter.findSoundByName(soundName);
            if (item != null) {
                showVolumeSlider(soundName, item.getVolume());
            }
        } else {
            hideVolumeSlider();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}
