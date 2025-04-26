package com.example.yogaadmin.course;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class YogaCourse {
    private int id;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private int capacity;
    private int duration;
    private float price;
    private String type;
    private String description;
    private Date startDate;
    private Date endDate;
    private Date createdAt;

    // Pre-formatted strings
    private String formattedStartTime;
    private String formattedEndTime;
    private String formattedDateRange;
    private String formattedCreatedAt;

    public YogaCourse(int id, String dayOfWeek, String startTime, String endTime,
                      int capacity, int duration, float price, String type,
                      String description, Date startDate, Date endDate) {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.duration = duration;
        this.price = price;
        this.type = type;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = new Date();
        updateFormattedStrings();
    }

    // Getters
    public int getId() { return id; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public int getCapacity() { return capacity; }
    public int getDuration() { return duration; }
    public float getPrice() { return price; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public Date getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        updateFormattedStrings();
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
        updateFormattedStrings();
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
        updateFormattedStrings();
    }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setPrice(float price) { this.price = price; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
        updateFormattedStrings();
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
        updateFormattedStrings();
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        updateFormattedStrings();
    }

    // Helper method to update formatted strings
    private void updateFormattedStrings() {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            // Format time range
            String start = timeFormat.format(new SimpleDateFormat("HH:mm").parse(startTime));
            String end = timeFormat.format(new SimpleDateFormat("HH:mm").parse(endTime));
            this.formattedStartTime = start;
            this.formattedEndTime = end;

            // Format date range
            this.formattedDateRange = dateFormat.format(startDate) + " to " + dateFormat.format(endDate);
            this.formattedCreatedAt = dateFormat.format(createdAt);
        } catch (Exception e) {
            // Fallback to raw values if formatting fails
            this.formattedStartTime = startTime;
            this.formattedEndTime = endTime;
            this.formattedDateRange = startDate.toString() + " to " + endDate.toString();
            this.formattedCreatedAt = createdAt.toString();
        }
    }

    // Pre-formatted getters
    public String getFormattedTimeRange() {
        return formattedStartTime + " - " + formattedEndTime;
    }

    public String getFormattedDateRange() {
        return formattedDateRange;
    }

    public String getFormattedCreatedAt() {
        return formattedCreatedAt;
    }
}