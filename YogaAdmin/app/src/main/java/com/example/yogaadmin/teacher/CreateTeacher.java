package com.example.yogaadmin.teacher;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.yogaadmin.BaseActivity;
import com.example.yogaadmin.CloudFirebaseSync;
import com.example.yogaadmin.DatabaseHelper;
import com.example.yogaadmin.R;

public class CreateTeacher extends BaseActivity {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "CreateTeacher";

    private DatabaseHelper dbHelper;
    private CloudFirebaseSync firebaseSync;
    private Uri profileImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_teacher);

        dbHelper = new DatabaseHelper(this);
        firebaseSync = new CloudFirebaseSync(this);
        checkStoragePermission();
        setupImagePicker();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        setupViews();
    }

    private void checkStoragePermission() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_IMAGES :
                Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        }
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
                            showToast("Failed to load image");
                        }
                    }
                });
    }

    private void setupViews() {
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        Button btnCreateTeacher = findViewById(R.id.btnCreateTeacher);

        btnSelectImage.setOnClickListener(v -> {
            if (isClickAllowed()) {
                openImagePicker();
            }
        });

        btnCreateTeacher.setOnClickListener(v -> {
            if (isClickAllowed()) {
                validateAndCreateTeacher();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void validateAndCreateTeacher() {
        String name = ((EditText)findViewById(R.id.etName)).getText().toString().trim();
        String email = ((EditText)findViewById(R.id.etEmail)).getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            showToast("Name is required");
            return;
        }

        if (!isValidEmail(email)) {
            showToast("Valid email is required");
            return;
        }

        String imageUri = (profileImageUri != null) ? profileImageUri.toString() : "";
        saveTeacher(name, email, imageUri);
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void saveTeacher(String name, String email, String imageUri) {
        Teacher teacher = new Teacher(0, name, email, imageUri);
        long id = dbHelper.createTeacher(teacher);

        if (id != -1) {
            teacher.setId((int) id);
            firebaseSync.uploadTeacher(teacher);

            showToast("Teacher created successfully");
            setResult(RESULT_OK);
            finish();
        } else {
            showToast("Failed to save teacher");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}