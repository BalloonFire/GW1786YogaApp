package com.example.yogaadmin.schedule;

import android.content.Context;

import com.example.yogaadmin.DatabaseHelper;
import com.example.yogaadmin.teacher.Teacher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class YogaSchedule {
    private int id;
    private int courseId;
    private int teacherId;
    private Date date;
    private String comments;
    private Date createdAt;

    // Pre-formatted strings
    private String formattedDate;
    private String formattedDateTime;
    private String formattedCreatedAt;

    public YogaSchedule(int id, int courseId, int teacherId, Date date, String comments) {
        this.id = id;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.date = date;
        this.comments = comments;
        this.createdAt = new Date();
        updateFormattedStrings();
    }

    // Getters
    public int getId() { return id; }
    public int getCourseId() { return courseId; }
    public Date getDate() { return date; }
    public String getComments() { return comments; }
    public Date getCreatedAt() { return createdAt; }
    public int getTeacherId() { return teacherId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
        updateFormattedStrings();
    }
    public void setDate(Date date) {
        this.date = date;
        updateFormattedStrings();
    }
    public void setComments(String comments) {
        this.comments = comments;
        updateFormattedStrings();
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        updateFormattedStrings();
    }

    private void updateFormattedStrings() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE, MMM dd, yyyy hh:mm a", Locale.getDefault());

        this.formattedDate = date != null ? dateFormat.format(date) : "";
        this.formattedDateTime = date != null ? dateTimeFormat.format(date) : "";
        this.formattedCreatedAt = createdAt != null ? dateFormat.format(createdAt) : "";
    }

    // Pre-formatted getters
    public String getFormattedDate() {
        return formattedDate;
    }

    public String getFormattedDateTime() {
        return formattedDateTime;
    }

    public String getFormattedCreatedAt() {
        return formattedCreatedAt;
    }

    public String getFormattedDetails(Context context) {
        String teacherName = getTeacherName(context);
        return teacherName + (comments != null && !comments.isEmpty() ? " - " + comments : "");
    }

    public String getTeacherName(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Teacher teacher = dbHelper.getTeacher(teacherId);
        dbHelper.close();
        return teacher != null ? teacher.getName() : "Unknown Teacher";
    }
}