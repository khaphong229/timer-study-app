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
    public static final String ACTION_PLAY = "PLAY";
    public static final String ACTION_STOP = "STOP";
    public static final String EXTRA_RES_ID = "RES_ID";
    public static final String EXTRA_VOLUME = "VOLUME";
    private static final int NOTIF_ID = 1002;
    private static final String CHANNEL_ID = "audio_play_channel";

    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (ACTION_PLAY.equals(action)) {
            int resId = intent.getIntExtra(EXTRA_RES_ID, 0);
            float volume = intent.getFloatExtra(EXTRA_VOLUME, 0.5f);
            playAudio(resId, volume);
        } else if (ACTION_STOP.equals(action)) {
            stopAudio();
        }
        return START_STICKY;
    }

    private void playAudio(int resId, float volume) {
        stopAudio();
        if (resId != 0) {
            mediaPlayer = MediaPlayer.create(this, resId);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(volume, volume);
                mediaPlayer.start();
                startForeground(NOTIF_ID, buildNotification());
            }
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
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
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        stopAudio();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // Khi app bị vuốt khỏi đa nhiệm, dừng audio và tự hủy Service
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
