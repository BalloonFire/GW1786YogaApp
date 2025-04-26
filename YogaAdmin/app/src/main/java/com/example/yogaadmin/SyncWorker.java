package com.example.yogaadmin;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "Starting background sync with Firebase");
            new CloudFirebaseSync(getApplicationContext()).syncAllData();
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error during background sync", e);
            return Result.failure();
        }
    }
}