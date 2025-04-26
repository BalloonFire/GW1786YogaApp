package com.example.yogaadmin;

import android.app.Application;
import androidx.work.Configuration;
import androidx.work.WorkManager;

public class YogaAdminApp extends Application implements Configuration.Provider {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize WorkManager with default configuration
        WorkManager.initialize(this, new Configuration.Builder().build());
    }

    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build();
    }
}