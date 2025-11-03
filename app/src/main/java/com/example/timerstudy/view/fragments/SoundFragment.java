package com.example.timerstudy.view.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timerstudy.R;
import com.example.timerstudy.model.SoundItem;
import com.example.timerstudy.presenter.SoundPresenter;
import com.example.timerstudy.view.adapters.SoundAdapter;

import java.util.ArrayList;
import java.util.List;

public class SoundFragment extends Fragment implements SoundPresenter.SoundView {
    private RecyclerView musicRecyclerView;
    private RecyclerView whiteNoiseRecyclerView;
    private TextView asmrTab, musicTab;
    private SeekBar volumeSeekBar;
    private TextView volumeLabel;
    private View volumeContainer;
    private View uploadContainer;
    private Button uploadButton;
    private RecyclerView uploadedAudioRecyclerView;
    private SoundAdapter uploadedAudioAdapter;
    private ArrayList<SoundItem> uploadedAudioList = new ArrayList<>();
    private static final int REQUEST_CODE_PICK_AUDIO = 2001;

    private SoundPresenter presenter;
    private SoundAdapter musicAdapter;
    private SoundAdapter whiteNoiseAdapter;
    private String currentPlayingSoundName = null;
    private static final int REQUEST_MODIFY_AUDIO_SETTINGS = 1001;
    private static final int REQUEST_MODIFY_AUDIO_SETTINGS_INITIAL = 1002;
    private AudioManager audioManager;
    private android.media.MediaPlayer uploadedPlayer;
    private ImageButton btnRandomAudio;
    private ImageButton btnRepeatAudio;
    private boolean isRepeatMode = false; // false: autoplay, true: repeat
    private int currentTab = 1; // 0: asmr/white noise, 1: music, 2: upload
    private boolean isUpdatingSeekBar = false; // Prevent infinite loop
    private BroadcastReceiver volumeReceiver;
    private Handler volumeHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sound, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerViews();
        setupTabs();
        setupVolumeControl();

        presenter = new SoundPresenter(this, getContext());
        audioManager = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
        volumeHandler = new Handler(Looper.getMainLooper());

        // Setup volume change listener
        setupVolumeChangeListener();

        // Kiểm tra quyền audio khi khởi tạo fragment
        checkAudioPermissionOnStart();

        // Gọi restorePlayingState sau khi presenter đã được khởi tạo và view đã sẵn
        // sàng
        presenter.restorePlayingState();

        // Đồng bộ SeekBar với âm lượng thiết bị khi khởi tạo
        syncSeekBarWithDeviceVolume();
    }

    private void initViews(View view) {
        musicRecyclerView = view.findViewById(R.id.musicRecyclerView);
        whiteNoiseRecyclerView = view.findViewById(R.id.whiteNoiseRecyclerView);
        asmrTab = view.findViewById(R.id.asmrTab);
        musicTab = view.findViewById(R.id.musicTab);
        volumeSeekBar = view.findViewById(R.id.volumeSeekBar);
        volumeLabel = view.findViewById(R.id.volumeLabel);
        volumeContainer = view.findViewById(R.id.volumeContainer);
        uploadContainer = view.findViewById(R.id.uploadContainer);
        uploadButton = view.findViewById(R.id.uploadButton);
        uploadedAudioRecyclerView = view.findViewById(R.id.uploadedAudioRecyclerView);
        btnRandomAudio = view.findViewById(R.id.btnRandomAudio);
        btnRepeatAudio = view.findViewById(R.id.btnRepeatAudio);
    }

    private void setupRecyclerViews() {
        musicAdapter = new SoundAdapter(this::onSoundItemClick);
        musicRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        musicRecyclerView.setAdapter(musicAdapter);

        whiteNoiseAdapter = new SoundAdapter(this::onSoundItemClick);
        whiteNoiseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        whiteNoiseRecyclerView.setAdapter(whiteNoiseAdapter);

        uploadedAudioAdapter = new SoundAdapter(this::onUploadedAudioClick);
        uploadedAudioRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        uploadedAudioRecyclerView.setAdapter(uploadedAudioAdapter);
    }

    private void setupTabs() {
        asmrTab.setOnClickListener(v -> {
            showWhiteNoiseView();
            updateTabSelection(true);
            currentTab = 0; // Set to ASMR tab
        });

        musicTab.setOnClickListener(v -> {
            showMusicView();
            updateTabSelection(false);
            currentTab = 1; // Set to Music tab
        });

        TextView uploadTab = requireView().findViewById(R.id.uploadTab);
        uploadTab.setOnClickListener(v -> {
            showUploadView();
            updateTabSelectionUpload();
            currentTab = 2; // Set to Upload tab
        });

        showMusicView();
        updateTabSelection(false);
        currentTab = 1; // Default to Music tab
    }

    private void setupVolumeControl() {
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && currentPlayingSoundName != null && !isUpdatingSeekBar) {
                    float volume = progress / 100.0f;

                    // Chỉ cập nhật volume của MediaPlayer, không restart audio
                    updateAudioVolume(currentPlayingSoundName, volume);

                    if (ContextCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
                        setDeviceVolume(progress);
                    } else {
                        requestAudioPermission();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        volumeContainer.setVisibility(View.GONE);

        if (uploadButton != null) {
            uploadButton.setOnClickListener(v -> {
                pickAudioFile();
            });
        }

        if (btnRandomAudio != null) {
            btnRandomAudio.setOnClickListener(v -> {
                playRandomAudio();
            });
        }

        if (btnRepeatAudio != null) {
            btnRepeatAudio.setOnClickListener(v -> {
                toggleRepeatMode();
            });
            updateRepeatButtonUI();
        }
    }

    private void setDeviceVolume(int progress) {
        if (audioManager != null) {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int newVolume = (int) (progress / 100.0f * maxVolume);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        }
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[] { Manifest.permission.MODIFY_AUDIO_SETTINGS },
                REQUEST_MODIFY_AUDIO_SETTINGS);
    }

    private void checkAudioPermissionOnStart() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            showAudioPermissionDialog();
        }
    }

    private void showAudioPermissionDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Audio Control Permission")
                .setMessage(
                        "This app needs permission to control device volume for better audio experience. Would you like to grant this permission?")
                .setPositiveButton("Grant", (dialog, which) -> {
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[] { Manifest.permission.MODIFY_AUDIO_SETTINGS },
                            REQUEST_MODIFY_AUDIO_SETTINGS_INITIAL);
                })
                .setNegativeButton("Skip", (dialog, which) -> {
                    // User can still use the app without this permission
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MODIFY_AUDIO_SETTINGS || requestCode == REQUEST_MODIFY_AUDIO_SETTINGS_INITIAL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (volumeSeekBar != null) {
                    setDeviceVolume(volumeSeekBar.getProgress());
                }
            } else if (requestCode == REQUEST_MODIFY_AUDIO_SETTINGS_INITIAL) {
                // Show a toast or message that some features may be limited
                // Optional: you can show a message here
            }
        }
    }

    private void showMusicView() {
        musicRecyclerView.setVisibility(View.VISIBLE);
        whiteNoiseRecyclerView.setVisibility(View.GONE);
        uploadContainer.setVisibility(View.GONE);
        // Chỉ ẩn volume slider, không dừng âm thanh
        hideVolumeSlider();
    }

    private void showWhiteNoiseView() {
        musicRecyclerView.setVisibility(View.GONE);
        whiteNoiseRecyclerView.setVisibility(View.VISIBLE);
        uploadContainer.setVisibility(View.GONE);
        // Chỉ ẩn volume slider, không dừng âm thanh
        hideVolumeSlider();
    }

    private void showUploadView() {
        musicRecyclerView.setVisibility(View.GONE);
        whiteNoiseRecyclerView.setVisibility(View.GONE);
        uploadContainer.setVisibility(View.VISIBLE);
        // Chỉ ẩn volume slider, không dừng âm thanh
        hideVolumeSlider();
    }

    private void updateTabSelection(boolean isAsmrSelected) {
        TextView uploadTab = requireView().findViewById(R.id.uploadTab);
        if (isAsmrSelected) {
            asmrTab.setTextColor(getResources().getColor(R.color.blue_accent, null));
            musicTab.setTextColor(getResources().getColor(R.color.gray_text, null));
            uploadTab.setTextColor(getResources().getColor(R.color.gray_text, null));
        } else {
            asmrTab.setTextColor(getResources().getColor(R.color.gray_text, null));
            musicTab.setTextColor(getResources().getColor(R.color.blue_accent, null));
            uploadTab.setTextColor(getResources().getColor(R.color.gray_text, null));
        }
    }

    private void updateTabSelectionUpload() {
        asmrTab.setTextColor(getResources().getColor(R.color.gray_text, null));
        musicTab.setTextColor(getResources().getColor(R.color.gray_text, null));
        TextView uploadTab = requireView().findViewById(R.id.uploadTab);
        uploadTab.setTextColor(getResources().getColor(R.color.blue_accent, null));
    }

    private void onSoundItemClick(String soundName) {
        presenter.onSoundItemClicked(soundName);
    }

    private void onUploadedAudioClick(String soundName) {
        for (SoundItem item : uploadedAudioList) {
            if (item.getName().equals(soundName)) {
                playUploadedAudio(item);
                break;
            }
        }
    }

    private void playUploadedAudio(SoundItem item) {
        stopUploadedAudio();
        if (item.getUri() != null) {
            uploadedPlayer = android.media.MediaPlayer.create(getContext(), item.getUri());
            if (uploadedPlayer != null) {
                uploadedPlayer.setLooping(true);
                uploadedPlayer.setVolume(item.getVolume(), item.getVolume());
                uploadedPlayer.start();
                item.setPlaying(true);
                uploadedAudioAdapter.updatePlayingState(item.getName(), true);
                showVolumeSlider(item.getName(), item.getVolume());
            }
        }
    }

    private void stopUploadedAudio() {
        if (uploadedPlayer != null) {
            uploadedPlayer.stop();
            uploadedPlayer.release();
            uploadedPlayer = null;
            for (SoundItem item : uploadedAudioList) {
                if (item.isPlaying()) {
                    item.setPlaying(false);
                    uploadedAudioAdapter.updatePlayingState(item.getName(), false);
                }
            }
            hideVolumeSlider();
        }
    }

    private void pickAudioFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_PICK_AUDIO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_AUDIO && resultCode == android.app.Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri audioUri = data.getData();
                String audioName = getFileName(audioUri);
                SoundItem item = new SoundItem(audioName, 0, false, 0.5f);
                item.setUri(audioUri);
                uploadedAudioList.add(item);
                uploadedAudioAdapter.updateSounds(uploadedAudioList);
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = "Unknown";
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0)
                        result = cursor.getString(idx);
                }
            }
        } else {
            String path = uri.getPath();
            int cut = path.lastIndexOf('/');
            if (cut != -1)
                result = path.substring(cut + 1);
        }
        return result;
    }

    @Override
    public void updateSoundList(List<SoundItem> sounds) {
        List<SoundItem> musicSounds = sounds.subList(0, Math.min(10, sounds.size()));
        List<SoundItem> whiteNoiseSounds = sounds.size() > 10 ? sounds.subList(10, sounds.size())
                : sounds.subList(0, 0);

        musicAdapter.updateSounds(musicSounds);
        whiteNoiseAdapter.updateSounds(whiteNoiseSounds);
    }

    @Override
    public void showVolumeSlider(String soundName, float volume) {
        currentPlayingSoundName = soundName;
        volumeLabel.setText("Volume");

        isUpdatingSeekBar = true;
        volumeSeekBar.setProgress((int) (volume * 100));
        isUpdatingSeekBar = false;

        volumeContainer.setVisibility(View.VISIBLE);
        updateRepeatButtonUI();

        // Đồng bộ với âm lượng thiết bị
        syncSeekBarWithDeviceVolume();
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
        stopUploadedAudio();

        // Unregister volume receiver
        if (volumeReceiver != null) {
            try {
                requireContext().unregisterReceiver(volumeReceiver);
            } catch (Exception e) {
                // Receiver might already be unregistered
            }
        }

        if (volumeHandler != null) {
            volumeHandler.removeCallbacksAndMessages(null);
        }
    }

    private void playRandomAudio() {
        List<SoundItem> currentTabSounds = new ArrayList<>();

        switch (currentTab) {
            case 0: // ASMR/White Noise tab
                currentTabSounds.addAll(whiteNoiseAdapter.getSoundItems());
                break;
            case 1: // Music tab
                currentTabSounds.addAll(musicAdapter.getSoundItems());
                break;
            case 2: // Upload tab
                currentTabSounds.addAll(uploadedAudioList);
                break;
        }

        if (currentTabSounds.isEmpty())
            return;

        int idx = (int) (Math.random() * currentTabSounds.size());
        SoundItem item = currentTabSounds.get(idx);

        if (currentTab == 2) { // Upload tab
            playUploadedAudio(item);
        } else { // Music or ASMR tab
            presenter.onSoundItemClicked(item.getName());
        }
    }

    private void toggleRepeatMode() {
        isRepeatMode = !isRepeatMode;
        updateRepeatButtonUI();
    }

    private void updateRepeatButtonUI() {
        if (btnRepeatAudio != null) {
            if (isRepeatMode) {
                // Sửa lại icon repeat cho Android chuẩn
                // btnRepeatAudio.setImageResource(android.R.drawable.ic_menu_revert);
                btnRepeatAudio.setImageResource(R.drawable.repeat_24);
                btnRepeatAudio.setContentDescription("Repeat");
            } else {
                // btnRepeatAudio.setImageResource(android.R.drawable.ic_media_next);
                btnRepeatAudio.setImageResource(R.drawable.autoplay_24);
                btnRepeatAudio.setContentDescription("Autoplay");
            }
        }
    }

    private void syncSeekBarWithDeviceVolume() {
        if (audioManager != null && volumeSeekBar != null) {
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int progress = (int) ((currentVolume / (float) maxVolume) * 100);

            isUpdatingSeekBar = true;
            volumeSeekBar.setProgress(progress);
            isUpdatingSeekBar = false;
        }
    }

    private void updateAudioVolume(String soundName, float volume) {
        // Cập nhật volume cho uploaded audio nếu đang phát
        if (uploadedPlayer != null && uploadedPlayer.isPlaying()) {
            uploadedPlayer.setVolume(volume, volume);
            // Cập nhật volume trong SoundItem
            for (SoundItem item : uploadedAudioList) {
                if (item.getName().equals(soundName)) {
                    item.setVolume(volume);
                    break;
                }
            }
        } else {
            // Cập nhật volume cho audio từ service mà không restart
            presenter.updateVolumeOnly(soundName, volume);
        }
    }

    private void setupVolumeChangeListener() {
        volumeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("android.media.VOLUME_CHANGED_ACTION".equals(intent.getAction())) {
                    int streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                    if (streamType == AudioManager.STREAM_MUSIC) {
                        // Delay slightly to ensure volume change is processed
                        volumeHandler.postDelayed(() -> {
                            if (currentPlayingSoundName != null) {
                                syncSeekBarWithDeviceVolume();
                            }
                        }, 100);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        requireContext().registerReceiver(volumeReceiver, filter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Đồng bộ lại khi fragment resume
        syncSeekBarWithDeviceVolume();
    }
}
