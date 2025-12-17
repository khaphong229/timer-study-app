package com.example.timerstudy.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.timerstudy.data.local.database.entities.SessionEntity;
import com.example.timerstudy.data.repository.SessionRepository;

import java.util.List;

public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting SyncWorker...");

        com.example.timerstudy.utils.UserManager userManager = com.example.timerstudy.utils.UserManager.getInstance(getApplicationContext());
        com.example.timerstudy.model.User currentUser = userManager.getCurrentUser();

        if (currentUser == null || currentUser.getAccessToken() == null || currentUser.getAccessToken().isEmpty()) {
            Log.d(TAG, "User not logged in or no access token, skipping sync.");
            return Result.success();
        }

        String token = currentUser.getAccessToken();

        try {
            SessionRepository repository = SessionRepository.getInstance(getApplicationContext());
            List<SessionEntity> pendingSessions = repository.getUnsyncedSessionsSync();

            if (pendingSessions == null || pendingSessions.isEmpty()) {
                Log.d(TAG, "No pending sessions to sync.");
                return Result.success();
            }

            Log.d(TAG, "Found " + pendingSessions.size() + " pending sessions.");

            boolean allSuccess = true;
            for (SessionEntity session : pendingSessions) {
                boolean success = repository.syncSessionSynchronous(session, token);
                if (!success) {
                    allSuccess = false;
                }
            }

            if (allSuccess) {
                Log.d(TAG, "All sessions synced successfully.");
                return Result.success();
            } else {
                Log.w(TAG, "Some sessions failed to sync.");
                return Result.retry();
            }

        } catch (Exception e) {
            Log.e(TAG, "Unexpected error", e);
            return Result.failure();
        }
    }

}

