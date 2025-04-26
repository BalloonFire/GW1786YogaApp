package com.example.yogaadmin;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class CloudChangeListener extends ContentObserver {
    private static final String TAG = "CloudChangeListener";
    private final Context context;

    public CloudChangeListener(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.d(TAG, "Database change detected, initiating sync");
        new CloudFirebaseSync(context).syncAllData();
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        Log.d(TAG, "Database change detected at URI: " + uri + ", initiating sync");
        new CloudFirebaseSync(context).syncAllData();
    }
}