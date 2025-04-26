package com.example.yogaadmin.schedule;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
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
import androidx.appcompat.app.AlertDialog;

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

public class EditSchedule extends BaseActivity {
    private DatabaseHelper dbHelper;
    private CloudFirebaseSync firebaseSync;
    private YogaSchedule currentSchedule;
    private YogaCourse currentCourse;
    private Calendar selectedDate = Calendar.getInstance();
    private Spinner spinnerTeachers;
    private EditText etComments;
    private Button btnDate, btnUpdate, btnDelete;

    private int selectedTeacherId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_yoga_schedule);

        dbHelper = new DatabaseHelper(this);
        firebaseSync = new CloudFirebaseSync(this);
        initializeViews();

        int scheduleId = getIntent().getIntExtra("schedule_id", -1);
        Log.d("EditSchedule", "Received schedule ID: " + scheduleId);
        if (scheduleId == -1) {
            Toast.makeText(this, "Schedule not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentSchedule = dbHelper.getSchedule(scheduleId);
        if (currentSchedule == null) {
            showErrorAndFinish("Error: Schedule not found");
            return;
        }

        currentCourse = dbHelper.getYogaCourse(currentSchedule.getCourseId());
        if (currentCourse == null) {
            showErrorAndFinish("Error: Associated course not found");
            return;
        }

        selectedDate.setTime(currentSchedule.getDate());
        selectedTeacherId = currentSchedule.getTeacherId();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        setupTeacherSpinner();
        populateFields();
        setupDatePicker();
        setupButtonListeners(); // Add this line
    }

    private void initializeViews() {
        spinnerTeachers = findViewById(R.id.spinnerTeachers);
        etComments = findViewById(R.id.etComments);
        btnDate = findViewById(R.id.btnDate);
        btnUpdate = findViewById(R.id.btnUpdateSchedule);
        btnDelete = findViewById(R.id.btnDeleteSchedule);

        setClickCooldown(btnDate, btnUpdate, btnDelete, etComments);
    }

    private void setupButtonListeners() {
        btnUpdate.setOnClickListener(v -> {
            if (isClickAllowed()) {
                updateSchedule();
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (isClickAllowed()) {
                deleteSchedule();
            }
        });
    }

    private void populateFields() {
        etComments.setText(currentSchedule.getComments());
        updateDateButton();
    }

    private void setupTeacherSpinner() {
        List<Teacher> teachers = dbHelper.getAllTeachers();

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

        if (selectedTeacherId > 0) {
            for (int i = 0; i < teachers.size(); i++) {
                if (teachers.get(i).getId() == selectedTeacherId) {
                    spinnerTeachers.setSelection(i);
                    break;
                }
            }
        }
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

    private void updateSchedule() {
        if (selectedTeacherId == -1) {
            Toast.makeText(this, "Please select a teacher", Toast.LENGTH_SHORT).show();
            return;
        }

        String comments = etComments.getText().toString().trim();
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
        if (!selectedDay.equalsIgnoreCase(currentCourse.getDayOfWeek())) {
            Toast.makeText(this, "Date must be a " + currentCourse.getDayOfWeek(), Toast.LENGTH_LONG).show();
            return;
        }

        YogaSchedule updatedSchedule = new YogaSchedule(
                currentSchedule.getId(),
                currentSchedule.getCourseId(),
                selectedTeacherId,
                selectedScheduleDate,
                comments
        );

        if (dbHelper.updateSchedule(updatedSchedule)) {
            firebaseSync.uploadSchedule(updatedSchedule);
            Toast.makeText(this, "Schedule updated", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to update schedule", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteSchedule() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Schedule")
                .setMessage("Are you sure you want to delete this schedule?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteSchedule(currentSchedule.getId())) {
                        firebaseSync.deleteSchedule(currentSchedule.getId());
                        Toast.makeText(this, "Schedule deleted", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete schedule", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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