package com.example.yogaadmin.course;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.yogaadmin.BaseActivity;
import com.example.yogaadmin.CloudFirebaseSync;
import com.example.yogaadmin.DatabaseHelper;
import com.example.yogaadmin.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditYogaCourse extends BaseActivity {
    private DatabaseHelper dbHelper;
    private CloudFirebaseSync firebaseSync;
    private YogaCourse currentCourse;
    private int courseId;

    private Spinner spDayOfWeek, spType;
    private EditText edDes, edPrice, edCapacity, edDuration;
    private Button btnStartDate, btnEndDate, btnStartTime, btnEndTime;

    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private Calendar startTimeCalendar = Calendar.getInstance();
    private Calendar endTimeCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_yoga_course);

        dbHelper = new DatabaseHelper(this);
        firebaseSync = new CloudFirebaseSync(this);
        initializeViews();

        // Get course ID passed from intent
        courseId = getIntent().getIntExtra("course_id", -1);
        if (courseId == -1) {
            showErrorAndFinish("Error: Course not found");
            return;
        }

        // Load course data
        currentCourse = dbHelper.getYogaCourse(courseId);
        if (currentCourse == null) {
            showErrorAndFinish("Error: Course not found");
            return;
        }

        // Populate fields with course data
        populateFields();
        setupDateAndTimePickers();
    }

    private void initializeViews() {
        spDayOfWeek = findViewById(R.id.spDayOfWeek);
        spType = findViewById(R.id.spType);
        edDes = findViewById(R.id.edmDes);
        edPrice = findViewById(R.id.edPrice);
        edCapacity = findViewById(R.id.edCapacity);
        edDuration = findViewById(R.id.edDuration);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnStartTime = findViewById(R.id.btnStartTime);
        btnEndTime = findViewById(R.id.btnEndTime);

        setClickCooldown(btnStartDate, btnEndDate, btnStartTime, btnEndTime, edDes, edPrice, edCapacity, edDuration);
    }

    private void populateFields() {
        setSpinnerSelection(spDayOfWeek, currentCourse.getDayOfWeek());
        setSpinnerSelection(spType, currentCourse.getType());

        edDes.setText(currentCourse.getDescription());
        edPrice.setText(String.valueOf(currentCourse.getPrice()));
        edCapacity.setText(String.valueOf(currentCourse.getCapacity()));
        edDuration.setText(String.valueOf(currentCourse.getDuration()));

        startDateCalendar.setTime(currentCourse.getStartDate());
        endDateCalendar.setTime(currentCourse.getEndDate());

        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date startTime = timeFormat.parse(currentCourse.getStartTime());
            Date endTime = timeFormat.parse(currentCourse.getEndTime());

            startTimeCalendar.setTime(startTime);
            endTimeCalendar.setTime(endTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateDateButtonTexts();
        updateTimeButtonTexts();
    }

    private void setupDateAndTimePickers() {
        btnStartDate.setOnClickListener(v -> {
            if (isClickAllowed()) {
                showDatePicker(startDateCalendar, (view, year, month, dayOfMonth) -> {
                    startDateCalendar.set(year, month, dayOfMonth);
                    updateDateButtonTexts();
                });
            }
        });

        btnEndDate.setOnClickListener(v -> {
            if (isClickAllowed()) {
                showDatePicker(endDateCalendar, (view, year, month, dayOfMonth) -> {
                    endDateCalendar.set(year, month, dayOfMonth);
                    updateDateButtonTexts();
                });
            }
        });

        btnStartTime.setOnClickListener(v -> {
            if (isClickAllowed()) {
                showTimePicker(startTimeCalendar, (view, hourOfDay, minute) -> {
                    startTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    startTimeCalendar.set(Calendar.MINUTE, minute);
                    updateTimeButtonTexts();
                    updateDuration();
                });
            }
        });

        btnEndTime.setOnClickListener(v -> {
            if (isClickAllowed()) {
                showTimePicker(endTimeCalendar, (view, hourOfDay, minute) -> {
                    endTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    endTimeCalendar.set(Calendar.MINUTE, minute);
                    updateTimeButtonTexts();
                    updateDuration();
                });
            }
        });
    }

    private void showDatePicker(Calendar calendar, DatePickerDialog.OnDateSetListener listener) {
        new DatePickerDialog(this, listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(Calendar calendar, TimePickerDialog.OnTimeSetListener listener) {
        new TimePickerDialog(this, listener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false).show();
    }

    private void updateDateButtonTexts() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        btnStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
        btnEndDate.setText(dateFormat.format(endDateCalendar.getTime()));
    }

    private void updateTimeButtonTexts() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        btnStartTime.setText(timeFormat.format(startTimeCalendar.getTime()));
        btnEndTime.setText(timeFormat.format(endTimeCalendar.getTime()));
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    public void onClickUpdateYogaCourse(View v) {
        if (!isClickAllowed()) return;

        try {
            String dayOfWeek = spDayOfWeek.getSelectedItem().toString();
            String type = spType.getSelectedItem().toString();
            String description = edDes.getText().toString().trim();
            String priceStr = edPrice.getText().toString().trim();
            String capacityStr = edCapacity.getText().toString().trim();

            // Validate inputs
            float price;
            int capacity;

            try {
                price = Float.parseFloat(priceStr);
                if (price < 0) throw new NumberFormatException();

                capacity = Integer.parseInt(capacityStr);
                if (capacity <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!endDateCalendar.after(startDateCalendar)) {
                Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            Calendar tempCal = (Calendar) startDateCalendar.clone();
            boolean dayExistsInRange = false;

            while (!tempCal.after(endDateCalendar)) {
                String currentDay = dayFormat.format(tempCal.getTime());
                if (currentDay.equalsIgnoreCase(dayOfWeek)) {
                    dayExistsInRange = true;
                    break;
                }
                tempCal.add(Calendar.DATE, 1);
            }

            if (!dayExistsInRange) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Toast.makeText(this,
                        "Selected day (" + dayOfWeek + ") must exist between " +
                                dateFormat.format(startDateCalendar.getTime()) + " and " +
                                dateFormat.format(endDateCalendar.getTime()),
                        Toast.LENGTH_LONG).show();
                return;
            }

            long timeDiff = endTimeCalendar.getTimeInMillis() - startTimeCalendar.getTimeInMillis();
            int duration = (int) (timeDiff / (1000 * 60));

            if (duration < 10) {
                Toast.makeText(this, "Duration must be at least 10 minutes", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update the course with all fields
            YogaCourse updatedCourse = new YogaCourse(
                    courseId,
                    dayOfWeek,
                    new SimpleDateFormat("HH:mm", Locale.getDefault()).format(startTimeCalendar.getTime()),
                    new SimpleDateFormat("HH:mm", Locale.getDefault()).format(endTimeCalendar.getTime()),
                    capacity,
                    duration,
                    price,
                    type,
                    description,
                    startDateCalendar.getTime(),
                    endDateCalendar.getTime()
            );

            int rowsAffected = dbHelper.updateYogaCourse(updatedCourse);
            if (rowsAffected > 0) {
                // Sync with Firebase
                firebaseSync.uploadCourse(updatedCourse);

                Toast.makeText(this, "Course updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to update course", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void updateDuration() {
        long timeDiff = endTimeCalendar.getTimeInMillis() - startTimeCalendar.getTimeInMillis();
        int duration = (int) (timeDiff / (1000 * 60));
        edDuration.setText(String.valueOf(duration));
    }

    public void onClickClearEdit(View v) {
        if (!isClickAllowed()) return;

        populateFields();
        Toast.makeText(this, "Changes cleared", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}