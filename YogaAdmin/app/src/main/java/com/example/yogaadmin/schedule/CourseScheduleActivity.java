package com.example.yogaadmin.schedule;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.yogaadmin.BaseActivity;
import com.example.yogaadmin.CloudFirebaseSync;
import com.example.yogaadmin.DatabaseHelper;
import com.example.yogaadmin.R;
import com.example.yogaadmin.course.YogaCourse;

import java.util.List;

public class CourseScheduleActivity extends BaseActivity {
    private DatabaseHelper dbHelper;
    private CloudFirebaseSync firebaseSync;
    private YogaCourse course;
    private List<YogaSchedule> schedules;
    private ScheduleAdapter adapter;
    private ActivityResultLauncher<Intent> createScheduleLauncher;
    private ActivityResultLauncher<Intent> editScheduleLauncher;
    private Button btnAddSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_yoga_schedule);

        dbHelper = new DatabaseHelper(this);
        firebaseSync = new CloudFirebaseSync(this);

        // Initialize launchers
        createScheduleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        refreshScheduleList();
                    }
                });

        editScheduleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        refreshScheduleList();
                    }
                });

        int courseId = getIntent().getIntExtra("course_id", -1);
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

        setupViews();
        refreshScheduleList();
    }

    private void setupViews() {
        TextView tvTitle = findViewById(R.id.tvScheduleTitle);
        tvTitle.setText(course.getType() + " Schedules");

        // Initialize the button and set click protection
        btnAddSchedule = findViewById(R.id.btnAddSchedule);
        setClickCooldown(btnAddSchedule);

        ListView lvSchedules = findViewById(R.id.lvSchedules);
        lvSchedules.setOnItemClickListener((parent, view, position, id) -> {
            if (isClickAllowed()) {
                YogaSchedule schedule = schedules.get(position);
                Log.d("ScheduleClick", "Attempting to edit schedule ID: " + schedule.getId());
                Intent intent = new Intent(this, EditSchedule.class);
                intent.putExtra("schedule_id", schedule.getId());
                editScheduleLauncher.launch(intent);
            }
        });
    }

    private void refreshScheduleList() {
        schedules = dbHelper.getSchedulesForCourse(course.getId());
        adapter = new ScheduleAdapter(this, schedules);
        ((ListView) findViewById(R.id.lvSchedules)).setAdapter(adapter);
    }

    @Override
    protected void onProtectedClick(View v) {
        if (v.getId() == R.id.btnAddSchedule) {
            Intent intent = new Intent(this, CreateSchedule.class);
            intent.putExtra("course_id", course.getId());
            createScheduleLauncher.launch(intent);
        }
    }

    private class ScheduleAdapter extends ArrayAdapter<YogaSchedule> {
        public ScheduleAdapter(Context context, List<YogaSchedule> schedules) {
            super(context, R.layout.yoga_schedule_item, schedules);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            YogaSchedule schedule = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.yoga_schedule_item, parent, false);
            }

            TextView tvDateTime = convertView.findViewById(R.id.tvDateTime);
            TextView tvTeacher = convertView.findViewById(R.id.tvTeacher);
            TextView tvComments = convertView.findViewById(R.id.tvComments);
            TextView tvCreatedAt = convertView.findViewById(R.id.tvCreatedAt);

            tvDateTime.setText(schedule.getFormattedDateTime());
            tvTeacher.setText("Teacher: " + schedule.getTeacherName(getContext()));
            tvComments.setText(schedule.getComments() != null && !schedule.getComments().isEmpty()
                    ? "Notes: " + schedule.getComments() : "No additional notes");
            tvCreatedAt.setText("Created: " + schedule.getFormattedCreatedAt());

            return convertView;
        }
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