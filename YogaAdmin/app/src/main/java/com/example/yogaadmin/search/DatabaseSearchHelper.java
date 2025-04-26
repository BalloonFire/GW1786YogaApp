package com.example.yogaadmin.search;

import android.content.Context;
import android.util.Log;

import com.example.yogaadmin.DatabaseHelper;
import com.example.yogaadmin.course.YogaCourse;
import com.example.yogaadmin.teacher.Teacher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DatabaseSearchHelper {
    private DatabaseHelper dbHelper;
    private Context context;

    public DatabaseSearchHelper(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    // Search across teachers and courses
    public SearchResults searchAll(String query) {
        SearchResults results = new SearchResults();
        try {
            results.teachers = searchTeachers(query);
            results.courses = searchCourses(query);
        } catch (Exception e) {
            Log.e("DatabaseSearchHelper", "Error during search", e);
        }
        return results;
    }

    // Search teachers by name or email
    public List<Teacher> searchTeachers(String query) {
        List<Teacher> allTeachers = dbHelper.getAllTeachers();
        List<Teacher> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return allTeachers;
        }

        String searchTerm = query.toLowerCase().trim();
        for (Teacher teacher : allTeachers) {
            if (teacher.getName().toLowerCase().contains(searchTerm)) {
                results.add(teacher);
            } else if (teacher.getEmail() != null &&
                    teacher.getEmail().toLowerCase().contains(searchTerm)) {
                results.add(teacher);
            }
        }
        return results;
    }

    // Search courses by type, description, day of week, or date
    public List<YogaCourse> searchCourses(String query) {
        List<YogaCourse> allCourses = dbHelper.getAllYogaCourses();
        List<YogaCourse> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return allCourses;
        }

        String searchTerm = query.toLowerCase().trim();
        for (YogaCourse course : allCourses) {
            if (course.getType().toLowerCase().contains(searchTerm)) {
                results.add(course);
            } else if (course.getDescription() != null &&
                    course.getDescription().toLowerCase().contains(searchTerm)) {
                results.add(course);
            } else if (course.getDayOfWeek().toLowerCase().contains(searchTerm)) {
                results.add(course);
            }
        }
        return results;
    }

    // Search courses by date patterns
    public List<YogaCourse> searchCoursesByDate(String query, SimpleDateFormat[] dateFormats, List<YogaCourse> textResults) {
        List<YogaCourse> allCourses = dbHelper.getAllYogaCourses();
        List<YogaCourse> dateResults = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return dateResults;
        }

        String searchTerm = query.toLowerCase().trim();

        // Create a set of course IDs that already matched text search
        Set<Integer> existingIds = new HashSet<>();
        for (YogaCourse course : textResults) {
            existingIds.add(course.getId());
        }

        for (YogaCourse course : allCourses) {
            // Skip if already in text results
            if (existingIds.contains(course.getId())) {
                continue;
            }

            // Check day of week
            if (course.getDayOfWeek().toLowerCase().contains(searchTerm)) {
                dateResults.add(course);
                continue;
            }

            // Check date formats against both start and end dates
            for (SimpleDateFormat format : dateFormats) {
                try {
                    String formattedStartDate = format.format(course.getStartDate()).toLowerCase();
                    String formattedEndDate = format.format(course.getEndDate()).toLowerCase();

                    if (formattedStartDate.contains(searchTerm) ||
                            formattedEndDate.contains(searchTerm)) {
                        dateResults.add(course);
                        break;
                    }
                } catch (Exception e) {
                    Log.d("DateSearch", "Skipping date format for query: " + query);
                }
            }
        }

        return dateResults;
    }

    // Container class for search results
    public static class SearchResults {
        public List<Teacher> teachers = new ArrayList<>();
        public List<YogaCourse> courses = new ArrayList<>();

        public boolean isEmpty() {
            return teachers.isEmpty() && courses.isEmpty();
        }
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}