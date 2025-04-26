package com.example.yogaadmin;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    private long lastClickTime = 0;
    private static final long clickcooldown_MS = 1000; // 1 second cooldown

    // Universal click checker
    protected boolean isClickAllowed() {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime < clickcooldown_MS) {
            Log.d("ClickProtection", "Click blocked - cooldown active");
            return false;
        }
        lastClickTime = currentTime;
        return true;
    }

    // Auto-protect multiple views
    protected void setClickCooldown(View... views) {
        for (View view : views) {
            view.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    onProtectedClick(v);
                }
            });
        }
    }

    // Override this in child activities
    protected void onProtectedClick(View v) {}
}
