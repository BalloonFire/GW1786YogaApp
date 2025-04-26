package com.example.yogaadmin.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yogaadmin.BaseActivity;
import com.example.yogaadmin.BottomHeader;
import com.example.yogaadmin.R;
import com.example.yogaadmin.course.CourseInfoActivity;
import com.example.yogaadmin.MainActivity;
import com.example.yogaadmin.course.YogaCourse;
import com.example.yogaadmin.teacher.EditTeacher;
import com.example.yogaadmin.teacher.Teacher;
import com.example.yogaadmin.teacher.TeacherActivity;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends BaseActivity {
    private RecyclerView rvSearchResults;
    private EditText etSearch;
    private ImageButton btnSearch;
    private Spinner spinnerSearchType, spinnerSortOrder;
    private DatabaseSearchHelper searchHelper;
    private BottomHeader bottomHeader;

    private final SimpleDateFormat[] dateFormats = {
            new SimpleDateFormat("MM/dd", Locale.getDefault()),    // 04/30
            new SimpleDateFormat("MMM d", Locale.getDefault()),    // Apr 30
            new SimpleDateFormat("MMMM d", Locale.getDefault()),   // April 30
            new SimpleDateFormat("EEEE", Locale.getDefault())      // Monday
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_search);

        initializeViews();
        setupRecyclerView();
        setupDatabaseHelper();
        setupSpinners();
        setupSearchHandlers();
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomHeader = new BottomHeader(this);
        bottomHeader.setupBottomNavigation();
    }

    private void initializeViews() {
        rvSearchResults = findViewById(R.id.rvSearchResults);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        spinnerSearchType = findViewById(R.id.spinnerSearchType);
        spinnerSortOrder = findViewById(R.id.spinnerSortOrder);
    }

    private void setupRecyclerView() {
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupDatabaseHelper() {
        searchHelper = new DatabaseSearchHelper(this);
    }

    private void setupSpinners() {
        // Search type spinner
        ArrayAdapter<CharSequence> searchTypeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.search_types,
                android.R.layout.simple_spinner_item
        );
        searchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSearchType.setAdapter(searchTypeAdapter);

        // Sort order spinner
        ArrayAdapter<CharSequence> sortOrderAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.sort_orders,
                android.R.layout.simple_spinner_item
        );
        sortOrderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortOrder.setAdapter(sortOrderAdapter);
    }

    private void setupSearchHandlers() {
        // Button click handler
        btnSearch.setOnClickListener(v -> performSearchWithDateSupport());

        // Keyboard action handler
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearchWithDateSupport();
                return true;
            }
            return false;
        });
    }

    private void performSearchWithDateSupport() {
        String query = etSearch.getText().toString().trim();
        String searchType = spinnerSearchType.getSelectedItem().toString();
        String sortOrder = spinnerSortOrder.getSelectedItem().toString();

        CombinedSearchAdapter adapter = new CombinedSearchAdapter(new CombinedSearchAdapter.OnItemClickListener() {
            @Override
            public void onTeacherClicked(Teacher teacher) {
                showTeacherDetails(teacher);
            }

            @Override
            public void onCourseClicked(YogaCourse course) {
                showCourseDetails(course);
            }
        });

        rvSearchResults.setAdapter(adapter);

        if (searchType.equals("Teachers")) {
            handleTeacherSearch(query, sortOrder, adapter);
        } else {
            handleCourseSearch(query, sortOrder, adapter);
        }
    }

    private void handleTeacherSearch(String query, String sortOrder, CombinedSearchAdapter adapter) {
        List<Teacher> teachers = searchHelper.searchTeachers(query);
        if (sortOrder.equals("Oldest First")) {
            Collections.reverse(teachers);
        }
        adapter.setTeachers(teachers);
    }

    private void handleCourseSearch(String query, String sortOrder, CombinedSearchAdapter adapter) {
        // First get text-based results
        List<YogaCourse> courses = searchHelper.searchCourses(query);

        // Then get date-based results, excluding duplicates
        if (!query.isEmpty()) {
            List<YogaCourse> dateResults = searchHelper.searchCoursesByDate(query, dateFormats, courses);
            courses.addAll(dateResults);
        }

        if (sortOrder.equals("Oldest First")) {
            Collections.reverse(courses);
        }
        adapter.setCourses(courses);
    }

    private void showTeacherDetails(Teacher teacher) {
        Intent intent = new Intent(this, EditTeacher.class);
        intent.putExtra("teacher_id", teacher.getId());
        startActivity(intent);
    }

    private void showCourseDetails(YogaCourse course) {
        Intent intent = new Intent(this, CourseInfoActivity.class);
        intent.putExtra("course_id", course.getId());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        searchHelper.close();
        super.onDestroy();
    }
}