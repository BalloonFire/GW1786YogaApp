package com.example.yogaadmin;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.yogaadmin.search.SearchActivity;
import com.example.yogaadmin.setting.SettingActivity;
import com.example.yogaadmin.teacher.TeacherActivity;

public class BottomHeader {
    private final BaseActivity activity;

    public BottomHeader(BaseActivity activity) {
        this.activity = activity;
    }

    public void setupBottomNavigation() {
        View bottomNav = activity.findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            View navTeachers = bottomNav.findViewById(R.id.nav_teachers);
            View navCourses = bottomNav.findViewById(R.id.nav_courses);
            View navSearch = bottomNav.findViewById(R.id.nav_search);
            View navSettings = bottomNav.findViewById(R.id.nav_settings);

            // Apply click protection
            activity.setClickCooldown(navTeachers, navCourses, navSearch, navSettings);

            // Highlight current tab
            if (isCurrentActivity(TeacherActivity.class)) {
                setActiveTab(navTeachers);
            } else if (isCurrentActivity(MainActivity.class)) {
                setActiveTab(navCourses);
            } else if (isCurrentActivity(SearchActivity.class)) {
                setActiveTab(navSearch);
            } else if (isCurrentActivity(SettingActivity.class)) {
                setActiveTab(navSettings);
                navSettings.setEnabled(false); // Disable settings button when in settings
            }

            navTeachers.setOnClickListener(v -> {
                if (!isCurrentActivity(TeacherActivity.class)) {
                    activity.startActivity(new Intent(activity, TeacherActivity.class));
                    activity.finish();
                }
            });

            navCourses.setOnClickListener(v -> {
                if (!isCurrentActivity(MainActivity.class)) {
                    activity.startActivity(new Intent(activity, MainActivity.class));
                    activity.finish();
                }
            });

            navSearch.setOnClickListener(v -> {
                if (!isCurrentActivity(SearchActivity.class)) {
                    activity.startActivity(new Intent(activity, SearchActivity.class));
                    activity.finish();
                }
            });

            navSettings.setOnClickListener(v -> {
                if (!isCurrentActivity(SettingActivity.class)) {
                    activity.startActivity(new Intent(activity, SettingActivity.class));
                    activity.finish();
                }
            });
        }
    }

    private void setActiveTab(View tab) {
        if (tab instanceof Button) {
            Button button = (Button) tab;
            button.setAlpha(1f);
            button.setTextColor(activity.getResources().getColor(R.color.blue_500));
        }
    }

    private <T> boolean isCurrentActivity(Class<T> activityClass) {
        return activity.getClass().equals(activityClass);
    }
}