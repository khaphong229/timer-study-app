package com.example.timerstudy.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.timerstudy.data.repository.ShopRepository;

public class ShopSyncWorker extends Worker {

    private static final String TAG = "ShopSyncWorker";

    public ShopSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting ShopSyncWorker...");
        ShopRepository repository = ShopRepository.getInstance(getApplicationContext());
        boolean success = repository.syncPendingPurchasesSync();
        
        if (success) {
            Log.d(TAG, "Shop sync completed successfully.");
            return Result.success();
        } else {
            Log.d(TAG, "Shop sync failed, retrying...");
            return Result.retry();
        }
    }
}
