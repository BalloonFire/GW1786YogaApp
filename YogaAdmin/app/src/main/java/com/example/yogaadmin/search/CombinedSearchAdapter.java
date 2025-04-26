package com.example.yogaadmin.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yogaadmin.R;
import com.example.yogaadmin.course.YogaCourse;
import com.example.yogaadmin.teacher.Teacher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CombinedSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_TEACHER = 1;
    private static final int TYPE_COURSE = 2;

    public interface OnItemClickListener {
        void onTeacherClicked(Teacher teacher);
        void onCourseClicked(YogaCourse course);
    }

    private List<Teacher> teachers = new ArrayList<>();
    private List<YogaCourse> courses = new ArrayList<>();
    private final OnItemClickListener listener;

    public CombinedSearchAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setTeachers(List<Teacher> teachers) {
        this.teachers = new ArrayList<>(teachers);
        this.courses.clear();
        notifyDataSetChanged();
    }

    public void setCourses(List<YogaCourse> courses) {
        this.courses = new ArrayList<>(courses);
        this.teachers.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return !teachers.isEmpty() ? TYPE_TEACHER : TYPE_COURSE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_TEACHER) {
            View view = inflater.inflate(R.layout.teacher_list_item, parent, false);
            return new TeacherViewHolder(view, listener);
        } else {
            View view = inflater.inflate(R.layout.yoga_course_item, parent, false);
            return new CourseViewHolder(view, listener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TeacherViewHolder) {
            ((TeacherViewHolder) holder).bind(teachers.get(position));
        } else if (holder instanceof CourseViewHolder) {
            ((CourseViewHolder) holder).bind(courses.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return !teachers.isEmpty() ? teachers.size() : courses.size();
    }

    static class TeacherViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvEmail;
        private final ImageView ivProfile;
        private final OnItemClickListener listener;

        TeacherViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            ivProfile = itemView.findViewById(R.id.ivProfile);
        }

        void bind(Teacher teacher) {
            tvName.setText(teacher.getName());
            tvEmail.setText(teacher.getEmail());

            Glide.with(itemView.getContext())
                    .load(teacher.getProfilePicturePath())
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .into(ivProfile);

            itemView.setOnClickListener(v -> listener.onTeacherClicked(teacher));
        }
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvType, tvDayOfWeek, tvTimeRange, tvDateRange;
        private final TextView tvCapacity, tvDuration, tvDescription, tvPrice;
        private final OnItemClickListener listener;

        CourseViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            tvType = itemView.findViewById(R.id.tvType);
            tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
            tvDateRange = itemView.findViewById(R.id.tvDateRange);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }

        void bind(YogaCourse course) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            String startDate = dateFormat.format(course.getStartDate());
            String endDate = dateFormat.format(course.getEndDate());

            tvType.setText(course.getType());
            tvDayOfWeek.setText(course.getDayOfWeek());
            tvTimeRange.setText(String.format("%s - %s", course.getStartTime(), course.getEndTime()));
            tvDateRange.setText(String.format("%s to %s", startDate, endDate));
            tvCapacity.setText(String.valueOf(course.getCapacity()));
            tvDuration.setText(String.format(Locale.getDefault(), "%d mins", course.getDuration()));
            tvDescription.setText(course.getDescription());
            tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", course.getPrice()));

            itemView.setOnClickListener(v -> listener.onCourseClicked(course));
        }
    }
}