package com.example.yogaadmin.teacher;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.yogaadmin.BaseActivity;
import com.example.yogaadmin.BottomHeader;
import com.example.yogaadmin.DatabaseHelper;
import com.example.yogaadmin.R;

import java.util.List;

public class TeacherActivity extends BaseActivity {
    private static final String TAG = "TeacherActivity";

    private DatabaseHelper dbHelper;
    private List<Teacher> teacherList;
    private TeacherListAdapter adapter;
    private BottomHeader bottomHeader;
    private Button btnAddTeacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_teacher);

        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupListView();
        setupBottomNavigation();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void initializeViews() {
        btnAddTeacher = findViewById(R.id.btnAddTeacher);
        setClickCooldown(btnAddTeacher);
    }

    private void setupListView() {
        ListView lvTeachers = findViewById(R.id.lvTeachers);
        teacherList = dbHelper.getAllTeachers();
        adapter = new TeacherListAdapter(this, teacherList);
        lvTeachers.setAdapter(adapter);

        lvTeachers.setOnItemClickListener((parent, view, position, id) -> {
            if (isClickAllowed()) {
                Teacher teacher = teacherList.get(position);
                navigateToEditTeacher(teacher.getId());
            }
        });
    }

    private void navigateToEditTeacher(int teacherId) {
        Intent intent = new Intent(TeacherActivity.this, EditTeacher.class);
        intent.putExtra("teacher_id", teacherId);
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        bottomHeader = new BottomHeader(this);
        bottomHeader.setupBottomNavigation();
    }

    @Override
    protected void onProtectedClick(View v) {
        if (v.getId() == R.id.btnAddTeacher) {
            startActivity(new Intent(this, CreateTeacher.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTeacherList();
    }

    private void refreshTeacherList() {
        List<Teacher> updatedList = dbHelper.getAllTeachers();
        teacherList.clear();
        teacherList.addAll(updatedList);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home && isClickAllowed()) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class TeacherListAdapter extends ArrayAdapter<Teacher> {
        private final DatabaseHelper localDbHelper;

        public TeacherListAdapter(@NonNull Context context, @NonNull List<Teacher> teachers) {
            super(context, 0, teachers);
            this.localDbHelper = new DatabaseHelper(context);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Teacher teacher = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.teacher_list_item, parent, false);
            }

            TextView tvName = convertView.findViewById(R.id.tvName);
            TextView tvEmail = convertView.findViewById(R.id.tvEmail);
            ImageView ivProfile = convertView.findViewById(R.id.ivProfile);

            if (teacher != null) {
                tvName.setText(teacher.getName());
                tvEmail.setText(teacher.getEmail());
                loadTeacherImage(teacher, ivProfile);
            }

            return convertView;
        }

        private void loadTeacherImage(Teacher teacher, ImageView imageView) {
            if (TextUtils.isEmpty(teacher.getProfilePicturePath())) {
                imageView.setImageResource(R.drawable.ic_default_profile);
                return;
            }

            try {
                Uri imageUri = Uri.parse(teacher.getProfilePicturePath());
                if (isUriValid(imageUri)) {
                    imageView.setImageURI(imageUri);
                } else {
                    handleInvalidImageUri(teacher, imageView);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading teacher image", e);
                imageView.setImageResource(R.drawable.ic_default_profile);
            }
        }

        private boolean isUriValid(Uri uri) {
            try {
                getContext().getContentResolver().openInputStream(uri).close();
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private void handleInvalidImageUri(Teacher teacher, ImageView imageView) {
            // Update database with empty image path
            Teacher updatedTeacher = new Teacher(
                    teacher.getId(),
                    teacher.getName(),
                    teacher.getEmail(),
                    ""
            );
            localDbHelper.updateTeacher(updatedTeacher);
            imageView.setImageResource(R.drawable.ic_default_profile);
        }
    }
}