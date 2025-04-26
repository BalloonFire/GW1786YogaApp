package com.example.yogaadmin;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.util.Log;

import com.example.yogaadmin.course.YogaCourse;
import com.example.yogaadmin.schedule.YogaSchedule;
import com.example.yogaadmin.teacher.Teacher;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CloudFirebaseSync {
    private static final String TAG = "CloudFirebaseSync";
    private static final String COURSES_NODE = "yoga_courses";
    private static final String SCHEDULES_NODE = "yoga_schedules";
    private static final String TEACHERS_NODE = "yoga_teachers";

    private final DatabaseHelper localDb;
    private final DatabaseReference firebaseDb;
    private final Context context;

    public CloudFirebaseSync(Context context) {
        this.context = context;
        this.localDb = new DatabaseHelper(context);
        this.firebaseDb = FirebaseDatabase.getInstance().getReference();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        }
        return false;
    }

    public void syncAllData() {
        if (!isNetworkAvailable()) {
            Log.w(TAG, "No network available for sync");
            return;
        }

        uploadAllCourses();
        uploadAllSchedules();
        uploadAllTeachers();
    }

    // Courses synchronization
    public void uploadAllCourses() {
        List<YogaCourse> courses = localDb.getAllYogaCourses();
        for (YogaCourse course : courses) {
            uploadCourse(course);
        }
    }

    public void uploadCourse(YogaCourse course) {
        firebaseDb.child(COURSES_NODE)
                .child(String.valueOf(course.getId()))
                .setValue(course)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Course uploaded successfully: " + course.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to upload course: " + course.getId(), e));
    }

    public void deleteCourse(int courseId) {
        firebaseDb.child(COURSES_NODE)
                .child(String.valueOf(courseId))
                .removeValue()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Course deleted successfully from Firebase: " + courseId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to delete course from Firebase: " + courseId, e));
    }

    // Schedules synchronization
    public void uploadAllSchedules() {
        List<YogaSchedule> schedules = localDb.getAllSchedules();
        for (YogaSchedule schedule : schedules) {
            uploadSchedule(schedule);
        }
    }

    public void uploadSchedule(YogaSchedule schedule) {
        firebaseDb.child(SCHEDULES_NODE)
                .child(String.valueOf(schedule.getId()))
                .setValue(schedule)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Schedule uploaded successfully: " + schedule.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to upload schedule: " + schedule.getId(), e));
    }

    public void deleteSchedule(int scheduleId) {
        firebaseDb.child(SCHEDULES_NODE)
                .child(String.valueOf(scheduleId))
                .removeValue()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Schedule deleted successfully from Firebase: " + scheduleId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to delete schedule from Firebase: " + scheduleId, e));
    }

    // Teachers synchronization
    public void uploadAllTeachers() {
        List<Teacher> teachers = localDb.getAllTeachers();
        for (Teacher teacher : teachers) {
            uploadTeacher(teacher);
        }
    }

    public void uploadTeacher(Teacher teacher) {
        firebaseDb.child(TEACHERS_NODE)
                .child(String.valueOf(teacher.getId()))
                .setValue(teacher)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Teacher uploaded successfully: " + teacher.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to upload teacher: " + teacher.getId(), e));
    }

    public void deleteTeacher(int teacherId) {
        firebaseDb.child(TEACHERS_NODE)
                .child(String.valueOf(teacherId))
                .removeValue()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Teacher deleted successfully from Firebase: " + teacherId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to delete teacher from Firebase: " + teacherId, e));
    }

    public void clearAllFirebaseData() {
        if (!isNetworkAvailable()) {
            Log.w(TAG, "No network available for clear operation");
            return;
        }

        // Remove all data from each node
        firebaseDb.child(COURSES_NODE).removeValue()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "All courses deleted from Firebase"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete courses from Firebase", e));

        firebaseDb.child(SCHEDULES_NODE).removeValue()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "All schedules deleted from Firebase"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete schedules from Firebase", e));

        firebaseDb.child(TEACHERS_NODE).removeValue()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "All teachers deleted from Firebase"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete teachers from Firebase", e));
    }
}