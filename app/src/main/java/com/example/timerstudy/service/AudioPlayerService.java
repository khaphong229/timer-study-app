package com.example.timerstudy.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.timerstudy.R;

public class AudioPlayerService extends Service {
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_SET_VOLUME = "ACTION_SET_VOLUME";
    public static final String ACTION_SET_REPEAT = "ACTION_SET_REPEAT";

    public static final String EXTRA_RES_ID = "EXTRA_RES_ID";
    public static final String EXTRA_VOLUME = "EXTRA_VOLUME";
    public static final String EXTRA_REPEAT_MODE = "EXTRA_REPEAT_MODE";
    public static final String EXTRA_SOUND_NAME = "EXTRA_SOUND_NAME";

    private static final int NOTIF_ID = 1002;
    private static final String CHANNEL_ID = "audio_play_channel";

    private MediaPlayer mediaPlayer;
    private boolean isRepeatMode = false;
    private String currentSoundName;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return START_NOT_STICKY;

        String action = intent.getAction();
        if (action == null)
            return START_NOT_STICKY;

        switch (action) {
            case ACTION_PLAY:
                int resId = intent.getIntExtra(EXTRA_RES_ID, -1);
                float volume = intent.getFloatExtra(EXTRA_VOLUME, 0.5f);
                isRepeatMode = intent.getBooleanExtra(EXTRA_REPEAT_MODE, false);
                currentSoundName = intent.getStringExtra(EXTRA_SOUND_NAME);
                playAudio(resId, volume);
                break;
            case ACTION_STOP:
                stopAudio();
                break;
            case ACTION_SET_VOLUME:
                float newVolume = intent.getFloatExtra(EXTRA_VOLUME, 0.5f);
                setVolume(newVolume);
                break;
            case ACTION_SET_REPEAT:
                isRepeatMode = intent.getBooleanExtra(EXTRA_REPEAT_MODE, false);
                if (mediaPlayer != null) {
                    mediaPlayer.setLooping(isRepeatMode);
                }
                break;
        }

        return START_NOT_STICKY;
    }

    private void playAudio(int resId, float volume) {
        stopAudio();

        try {
            mediaPlayer = MediaPlayer.create(this, resId);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(isRepeatMode);
                mediaPlayer.setVolume(volume, volume);

                mediaPlayer.setOnCompletionListener(mp -> {
                    if (!isRepeatMode) {
                        // Send broadcast when audio completed
                        Intent completionIntent = new Intent("com.example.timerstudy.AUDIO_COMPLETED");
                        completionIntent.putExtra("soundName", currentSoundName);
                        sendBroadcast(completionIntent);
                    }
                });

                mediaPlayer.start();
                startForeground(NOTIF_ID, buildNotification());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setVolume(float volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume);
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
            stopForeground(true);
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("TimerStudy")
                .setContentText("Đang phát âm thanh nền")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Audio Playback", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null)
                manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAudio();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopAudio();
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
