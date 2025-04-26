package com.example.yogaadmin.setting;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import com.example.yogaadmin.BaseActivity;
import com.example.yogaadmin.BottomHeader;
import com.example.yogaadmin.CloudFirebaseSync;
import com.example.yogaadmin.DatabaseHelper;
import com.example.yogaadmin.MainActivity;
import com.example.yogaadmin.R;
import com.example.yogaadmin.course.YogaCourse;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends BaseActivity {

    private static final String PREF_DARK_MODE = "dark_mode";
    private SwitchCompat switchDarkMode;
    private SharedPreferences sharedPreferences;
    private BottomHeader bottomHeader;
    private DatabaseHelper dbHelper;
    private CloudFirebaseSync firebaseSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        firebaseSync = new CloudFirebaseSync(this);

        sharedPreferences = getSharedPreferences("YogaAdminPrefs", MODE_PRIVATE);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        // Load current mode
        boolean isDarkMode = sharedPreferences.getBoolean(PREF_DARK_MODE, false);
        switchDarkMode.setChecked(isDarkMode);

        // Set switch listener
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setDarkMode(isChecked);
            saveDarkModePreference(isChecked);
            recreate(); // Recreate activity to apply theme changes
        });
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomHeader = new BottomHeader(this);
        bottomHeader.setupBottomNavigation();
    }

    private void setDarkMode(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void saveDarkModePreference(boolean isDarkMode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_DARK_MODE, isDarkMode);
        editor.apply();
    }

    public void onClickClearAllData(View v) {
        if (!isClickAllowed()) return;

        new AlertDialog.Builder(this)
                .setTitle("Clear All Data")
                .setMessage("Are you sure you want to delete ALL yoga courses, schedules, and teachers? This cannot be undone!")
                .setPositiveButton("DELETE ALL", (dialog, which) -> {
                    try {
                        // First delete all data from local database
                        dbHelper.clearAllData();

                        // Then clear all data from Firebase
                        firebaseSync.clearAllFirebaseData();

                        Toast.makeText(this, "All data has been cleared", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error clearing data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("MainActivity", "Data clearance error", e);
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Apply current theme when activity resumes
        boolean isDarkMode = sharedPreferences.getBoolean(PREF_DARK_MODE, false);
        setDarkMode(isDarkMode);
    }
}