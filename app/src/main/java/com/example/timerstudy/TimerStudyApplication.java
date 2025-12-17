package com.example.timerstudy;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.timerstudy.worker.SyncWorker;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * Custom Application class for initializing Facebook SDK
 * This must be initialized before any Facebook features are used
 */
public class TimerStudyApplication extends Application implements DefaultLifecycleObserver {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        // Register Lifecycle Observer to detect when app goes to background
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        // App moved to background -> Trigger Sync
        scheduleSync();
    }

    private void scheduleSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniqueWork(
                "SyncWorker",
                ExistingWorkPolicy.REPLACE,
                syncRequest
        );
    }
}




