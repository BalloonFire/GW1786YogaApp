package com.example.yogaadmin.course;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.yogaadmin.BaseActivity;
import com.example.yogaadmin.CloudFirebaseSync;
import com.example.yogaadmin.DatabaseHelper;
import com.example.yogaadmin.R;
import com.example.yogaadmin.schedule.CourseScheduleActivity;

public class CourseInfoActivity extends BaseActivity {
    private YogaCourse course;
    private DatabaseHelper dbHelper;
    private CloudFirebaseSync firebaseSync;
    private ActivityResultLauncher<Intent> editCourseLauncher;
    private ActivityResultLauncher<Intent> viewScheduleLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_yoga_course);

        dbHelper = new DatabaseHelper(this);
        firebaseSync = new CloudFirebaseSync(this);

        // Initialize ActivityResultLaunchers
        editCourseLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        refreshCourseData();
                    }
                });

        viewScheduleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {});

        // Get course ID from intent
        int courseId = getIntent().getIntExtra("course_id", -1);
        if (courseId == -1) {
            Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load course data
        course = dbHelper.getYogaCourse(courseId);
        if (course == null) {
            Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        initializeViews();
        setupProtectedViews();
    }

    private void initializeViews() {
        TextView tvType = findViewById(R.id.tvDetailType);
        TextView tvDay = findViewById(R.id.tvDetailDay);
        TextView tvTime = findViewById(R.id.tvDetailTime);
        TextView tvDateRange = findViewById(R.id.tvDetailDateRange);
        TextView tvDuration = findViewById(R.id.tvDetailDuration);
        TextView tvCapacity = findViewById(R.id.tvDetailCapacity);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);

        tvType.setText(course.getType());
        tvDay.setText(course.getDayOfWeek());
        tvTime.setText(course.getFormattedTimeRange());
        tvDateRange.setText(course.getFormattedDateRange());
        tvDuration.setText(String.format("%d minutes", course.getDuration()));
        tvCapacity.setText(String.valueOf(course.getCapacity()));
        tvPrice.setText(String.format("£%.2f", course.getPrice()));
        tvDescription.setText(course.getDescription() != null && !course.getDescription().isEmpty() ?
                course.getDescription() : "No description available");
    }

    private void setupProtectedViews() {
        Button btnViewSchedule = findViewById(R.id.btnViewSchedule);
        Button btnEdit = findViewById(R.id.btnEditCourse);
        Button btnDelete = findViewById(R.id.btnDeleteCourse);

        setClickCooldown(btnViewSchedule, btnEdit, btnDelete);

        btnDelete.setOnClickListener(v -> {
            if (isClickAllowed()) {
                showDeleteConfirmationDialog();
            }
        });
    }

    @Override
    protected void onProtectedClick(View v) {
        int id = v.getId();
        if (id == R.id.btnViewSchedule) {
            handleViewSchedule();
        } else if (id == R.id.btnEditCourse) {
            handleEditCourse();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete this course? This will also delete all related schedules.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    performCourseDeletion();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleViewSchedule() {
        Intent intent = new Intent(this, CourseScheduleActivity.class);
        intent.putExtra("course_id", course.getId());
        viewScheduleLauncher.launch(intent);
    }

    private void handleEditCourse() {
        Intent intent = new Intent(this, EditYogaCourse.class);
        intent.putExtra("course_id", course.getId());
        editCourseLauncher.launch(intent);
    }

    private void performCourseDeletion() {
        // First delete from local database
        if (dbHelper.deleteYogaCourse(course.getId())) {
            // Then delete from Firebase
            firebaseSync.deleteCourse(course.getId());

            Toast.makeText(this, "Course deleted", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to delete course", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshCourseData() {
        course = dbHelper.getYogaCourse(course.getId());
        if (course == null) {
            Toast.makeText(this, "Course no longer exists", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initializeViews();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (isClickAllowed()) {
                getOnBackPressedDispatcher().onBackPressed();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}