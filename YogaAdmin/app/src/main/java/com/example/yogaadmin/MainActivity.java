package com.example.yogaadmin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yogaadmin.course.CourseInfoActivity;
import com.example.yogaadmin.course.CreateYogaCourse;
import com.example.yogaadmin.course.YogaCourse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity {
    private List<YogaCourse> courseList = new ArrayList<>();
    private YogaCourseAdapter adapter;
    private DatabaseHelper dbHelper;
    private BottomHeader bottomHeader;
    private CloudFirebaseSync firebaseSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database and UI components
        dbHelper = new DatabaseHelper(this);
        firebaseSync = new CloudFirebaseSync(this);
        bottomHeader = new BottomHeader(this);
        bottomHeader.setupBottomNavigation();

        setupListView();
        initializeFirebaseSync();
        applyTheme();
    }

    private void applyTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences("YogaAdminPrefs", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void setupListView() {
        ListView lv = findViewById(R.id.lvCourse);
        lv.setOnItemClickListener((parent, view, position, id) -> {
            if (isClickAllowed()) {
                YogaCourse course = courseList.get(position);
                Intent intent = new Intent(MainActivity.this, CourseInfoActivity.class);
                intent.putExtra("course_id", course.getId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCourseList();
    }

    private void refreshCourseList() {
        courseList = dbHelper.getAllYogaCourses();
        ListView lv = findViewById(R.id.lvCourse);
        adapter = new YogaCourseAdapter(this, courseList);
        lv.setAdapter(adapter);
    }

    private void initializeFirebaseSync() {
        try {
            // Schedule periodic background sync
            schedulePeriodicSync();

            // Perform initial sync
            firebaseSync.syncAllData();
        } catch (Exception e) {
            Log.e("MainActivity", "Error initializing Firebase sync", e);
            Toast.makeText(this, "Sync initialization failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void schedulePeriodicSync() {
        try {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                    SyncWorker.class,
                    12, // Repeat every 12 hours
                    TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .build();

            WorkManager workManager = WorkManager.getInstance(this);
            workManager.enqueueUniquePeriodicWork(
                    "yoga_sync_work",
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest);
        } catch (Exception e) {
            Log.e("MainActivity", "Error scheduling periodic sync", e);
            Toast.makeText(this, "Failed to schedule sync", Toast.LENGTH_SHORT).show();
        }
    }

    public static class YogaCourseAdapter extends ArrayAdapter<YogaCourse> {
        public YogaCourseAdapter(Context context, List<YogaCourse> courses) {
            super(context, 0, courses);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            try {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.yoga_course_item, parent, false);
                    holder = new ViewHolder();
                    holder.tvDayOfWeek = convertView.findViewById(R.id.tvDayOfWeek);
                    holder.tvType = convertView.findViewById(R.id.tvType);
                    holder.tvTimeRange = convertView.findViewById(R.id.tvTimeRange);
                    holder.tvDateRange = convertView.findViewById(R.id.tvDateRange);
                    holder.tvCapacity = convertView.findViewById(R.id.tvCapacity);
                    holder.tvDuration = convertView.findViewById(R.id.tvDuration);
                    holder.tvDescription = convertView.findViewById(R.id.tvDescription);
                    holder.tvPrice = convertView.findViewById(R.id.tvPrice);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                final YogaCourse course = getItem(position);
                if (course == null) {
                    return convertView;
                }

                // Set all course data to views
                holder.tvDayOfWeek.setText(course.getDayOfWeek() != null ? course.getDayOfWeek() : "");
                holder.tvType.setText(course.getType() != null ? course.getType() : "N/A");
                holder.tvTimeRange.setText(course.getFormattedTimeRange() != null ? course.getFormattedTimeRange() : "N/A");
                holder.tvDateRange.setText(course.getFormattedDateRange() != null ? course.getFormattedDateRange() : "N/A");
                holder.tvCapacity.setText(String.format("%d", course.getCapacity()));
                holder.tvDuration.setText(String.format("%d mins", course.getDuration()));
                holder.tvDescription.setText(course.getDescription() != null ? course.getDescription() : "");
                holder.tvPrice.setText(String.format("$%.2f", course.getPrice()));

            } catch (Exception e) {
                Log.e("YogaCourseAdapter", "Error in getView", e);
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.yoga_course_item, parent, false);
                TextView errorView = convertView.findViewById(R.id.tvType);
                if (errorView != null) {
                    errorView.setText("Error loading course");
                }
            }

            return convertView;
        }

        class ViewHolder {
            TextView tvType, tvDayOfWeek, tvTimeRange, tvDateRange, tvCapacity, tvDuration, tvDescription, tvPrice;
        }
    }

    public void onCreateYogaCourse(View v) {
        if (isClickAllowed()) {
            startActivity(new Intent(this, CreateYogaCourse.class));
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}