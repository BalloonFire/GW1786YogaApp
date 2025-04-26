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
import java.util.Locale;

public class CreateYogaCourse extends BaseActivity {
    private Spinner spDayOfWeek, spType;
    private EditText edDes, edPrice, edCapacity, edDuration;
    private Button btnStartDate, btnEndDate, btnStartTime, btnEndTime;
    private DatabaseHelper dbHelper;
    private CloudFirebaseSync firebaseSync;

    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private Calendar startTimeCalendar = Calendar.getInstance();
    private Calendar endTimeCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_yoga_course);

        dbHelper = new DatabaseHelper(this);
        firebaseSync = new CloudFirebaseSync(this);
        initializeViews();
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

        updateDateButtonTexts();
        updateTimeButtonTexts();

        setClickCooldown(btnStartDate, btnEndDate, btnStartTime, btnEndTime, edDes, edPrice, edCapacity, edDuration);
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

    public void onClickCreateYogaCourse(View v) {
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

            YogaCourse newCourse = new YogaCourse(
                    -1,
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

            long id = dbHelper.createNewYogaCourse(newCourse);
            if (id != -1) {
                newCourse.setId((int) id);
                // Immediately sync to Firebase
                firebaseSync.uploadCourse(newCourse);

                Toast.makeText(this, "Course created successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to create course", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateDuration() {
        long timeDiff = endTimeCalendar.getTimeInMillis() - startTimeCalendar.getTimeInMillis();
        int duration = (int) (timeDiff / (1000 * 60));
        edDuration.setText(String.valueOf(duration));
    }

    public void onClickClearYogaCourse(View v) {
        if (!isClickAllowed()) return;

        spDayOfWeek.setSelection(0);
        spType.setSelection(0);
        edDes.setText("");
        edPrice.setText("");
        edCapacity.setText("");
        edDuration.setText("");

        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();
        startTimeCalendar = Calendar.getInstance();
        endTimeCalendar = Calendar.getInstance();

        updateDateButtonTexts();
        updateTimeButtonTexts();

        Toast.makeText(this, "Fields cleared", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}