package com.example.yogaadmin.schedule;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.example.yogaadmin.BaseActivity;
import com.example.yogaadmin.CloudFirebaseSync;
import com.example.yogaadmin.DatabaseHelper;
import com.example.yogaadmin.R;
import com.example.yogaadmin.course.YogaCourse;
import com.example.yogaadmin.teacher.Teacher;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateSchedule extends BaseActivity {
    private DatabaseHelper dbHelper;
    private CloudFirebaseSync firebaseSync;
    private YogaCourse currentCourse;
    private Calendar selectedDate = Calendar.getInstance();
    private Spinner spinnerTeachers;
    private EditText etComments;
    private Button btnDate;

    private int selectedTeacherId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_yoga_schedule);

        dbHelper = new DatabaseHelper(this);
        firebaseSync = new CloudFirebaseSync(this);
        initializeViews();

        int courseId = getIntent().getIntExtra("course_id", -1);
        if (courseId == -1) {
            showErrorAndFinish("Error: Course not found");
            return;
        }

        currentCourse = dbHelper.getYogaCourse(courseId);
        if (currentCourse == null) {
            showErrorAndFinish("Error: Course not found");
            return;
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        setupTeacherSpinner();
        setupDatePicker();
        updateDateButton();
    }

    private void initializeViews() {
        spinnerTeachers = findViewById(R.id.spinnerTeachers);
        etComments = findViewById(R.id.etComments);
        btnDate = findViewById(R.id.btnDate);

        setClickCooldown(btnDate, etComments);
    }

    private void setupTeacherSpinner() {
        List<Teacher> teachers = dbHelper.getAllTeachers();
        Teacher defaultTeacher = new Teacher(-1, "Select Teacher", "", "");
        teachers.add(0, defaultTeacher);

        ArrayAdapter<Teacher> adapter = new ArrayAdapter<Teacher>(this,
                android.R.layout.simple_spinner_item, teachers) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setText(teachers.get(position).getName());
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setText(teachers.get(position).getName());
                if (position == 0) {
                    textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeachers.setAdapter(adapter);

        spinnerTeachers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Teacher selected = (Teacher) parent.getItemAtPosition(position);
                selectedTeacherId = selected.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTeacherId = -1;
            }
        });
    }

    private void setupDatePicker() {
        btnDate.setOnClickListener(v -> {
            if (isClickAllowed()) {
                showDatePicker();
            }
        });
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateButton();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMinDate(currentCourse.getStartDate().getTime());
        dialog.getDatePicker().setMaxDate(currentCourse.getEndDate().getTime());
        dialog.show();
    }

    private void updateDateButton() {
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault());
        btnDate.setText(format.format(selectedDate.getTime()));
    }

    public void onClickCreateSchedule(View v) {
        if (!isClickAllowed()) return;

        if (selectedTeacherId == -1) {
            Toast.makeText(this, "Please select a teacher", Toast.LENGTH_SHORT).show();
            return;
        }

        String comments = etComments.getText().toString().trim();
        String courseDay = currentCourse.getDayOfWeek();
        Date selectedScheduleDate = selectedDate.getTime();

        if (selectedScheduleDate.before(currentCourse.getStartDate()) ||
                selectedScheduleDate.after(currentCourse.getEndDate())) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Toast.makeText(this,
                    "Date must be between " + dateFormat.format(currentCourse.getStartDate()) +
                            " and " + dateFormat.format(currentCourse.getEndDate()),
                    Toast.LENGTH_LONG).show();
            return;
        }

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        String selectedDay = dayFormat.format(selectedScheduleDate);

        if (!selectedDay.equalsIgnoreCase(courseDay)) {
            Toast.makeText(this, "Date must be a " + courseDay, Toast.LENGTH_LONG).show();
            return;
        }

        YogaSchedule schedule = new YogaSchedule(
                0,
                currentCourse.getId(),
                selectedTeacherId,
                selectedScheduleDate,
                comments
        );

        long id = dbHelper.createNewSchedule(schedule);
        if (id != -1) {
            schedule.setId((int) id);
            firebaseSync.uploadSchedule(schedule);

            Toast.makeText(this, "Schedule created", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to create schedule", Toast.LENGTH_SHORT).show();
        }
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}