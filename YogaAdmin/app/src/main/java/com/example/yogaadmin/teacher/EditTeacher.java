package com.example.yogaadmin.teacher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.example.yogaadmin.BaseActivity;
import com.example.yogaadmin.CloudFirebaseSync;
import com.example.yogaadmin.DatabaseHelper;
import com.example.yogaadmin.R;

public class EditTeacher extends BaseActivity {
    private static final String TAG = "EditTeacher";

    private DatabaseHelper dbHelper;
    private CloudFirebaseSync firebaseSync;
    private Teacher teacher;
    private Uri profileImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_teacher);

        dbHelper = new DatabaseHelper(this);
        firebaseSync = new CloudFirebaseSync(this);
        int teacherId = getIntent().getIntExtra("teacher_id", -1);
        teacher = dbHelper.getTeacher(teacherId);

        if (teacher == null) {
            Toast.makeText(this, "Teacher not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupImagePicker();
        setupViews();
        populateFields();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        profileImageUri = result.getData().getData();
                        try {
                            ((ImageView)findViewById(R.id.ivProfile)).setImageURI(profileImageUri);
                        } catch (Exception e) {
                            Log.e(TAG, "Image loading failed", e);
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupViews() {
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        Button btnUpdateTeacher = findViewById(R.id.btnUpdateTeacher);
        Button btnDeleteTeacher = findViewById(R.id.btnDeleteTeacher);

        btnSelectImage.setOnClickListener(v -> {
            if (isClickAllowed()) {
                openImagePicker();
            }
        });

        btnUpdateTeacher.setOnClickListener(v -> {
            if (isClickAllowed()) {
                updateTeacher();
            }
        });

        btnDeleteTeacher.setOnClickListener(v -> {
            if (isClickAllowed()) {
                deleteTeacher();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void populateFields() {
        ((EditText)findViewById(R.id.etName)).setText(teacher.getName());
        ((EditText)findViewById(R.id.etEmail)).setText(teacher.getEmail());

        if (!TextUtils.isEmpty(teacher.getProfilePicturePath())) {
            try {
                Uri uri = Uri.parse(teacher.getProfilePicturePath());
                if (isUriValid(uri)) {
                    profileImageUri = uri;
                    ((ImageView)findViewById(R.id.ivProfile)).setImageURI(uri);
                } else {
                    // URI is invalid, clear it
                    teacher.setProfilePicturePath("");
                    dbHelper.updateTeacher(teacher);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error loading profile image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isUriValid(Uri uri) {
        try {
            getContentResolver().openInputStream(uri).close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateTeacher() {
        String name = ((EditText)findViewById(R.id.etName)).getText().toString().trim();
        String email = ((EditText)findViewById(R.id.etEmail)).getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageUri = (profileImageUri != null) ? profileImageUri.toString() : teacher.getProfilePicturePath();
        updateTeacherInDatabase(name, email, imageUri);
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void updateTeacherInDatabase(String name, String email, String imageUri) {
        teacher.setName(name);
        teacher.setEmail(email);
        teacher.setProfilePicturePath(imageUri);

        int rowsAffected = dbHelper.updateTeacher(teacher);
        if (rowsAffected > 0) {
            firebaseSync.uploadTeacher(teacher);
            Toast.makeText(this, "Teacher updated", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to update teacher", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteTeacher() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Teacher")
                .setMessage("Are you sure you want to delete this teacher?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteTeacher(teacher.getId())) {
                        firebaseSync.deleteTeacher(teacher.getId());
                        Toast.makeText(this, "Teacher deleted", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete teacher", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}