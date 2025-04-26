package com.example.yogaadmin.teacher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Teacher {
    private int id;
    private String name;
    private String email;
    private String profilePicturePath;
    private Date createdAt;

    // Pre-formatted strings
    private String formattedCreatedAt;

    public Teacher(int id, String name, String email, String profilePicturePath) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profilePicturePath = profilePicturePath;
        this.createdAt = new Date();
        updateFormattedStrings();
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getProfilePicturePath() { return profilePicturePath; }
    public Date getCreatedAt() { return createdAt; }
    public String getFormattedCreatedAt() { return formattedCreatedAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) {
        this.name = name;
        updateFormattedStrings();
    }
    public void setEmail(String email) {
        this.email = email;
        updateFormattedStrings();
    }
    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
        updateFormattedStrings();
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        updateFormattedStrings();
    }

    private void updateFormattedStrings() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        this.formattedCreatedAt = dateFormat.format(createdAt);
    }

    public String getFormattedDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);

        if (email != null && !email.isEmpty()) {
            sb.append(" (").append(email).append(")");
        }

        return sb.toString();
    }
}