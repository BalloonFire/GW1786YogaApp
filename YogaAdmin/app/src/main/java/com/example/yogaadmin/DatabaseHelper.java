package com.example.yogaadmin;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.yogaadmin.course.YogaCourse;
import com.example.yogaadmin.schedule.YogaSchedule;
import com.example.yogaadmin.teacher.Teacher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "YogaDB";
    private static final int DATABASE_VERSION = 4;
    private static final String TABLE_YOGA_COURSE = "YogaCourse";
    private static final String TABLE_YOGA_SCHEDULE = "YogaSchedule";
    private static final String TABLE_TEACHERS = "YogaTeacher";

    // Format for database storage (ISO 8601 compatible)
    private static final SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_YOGACOURSE = "CREATE TABLE " + TABLE_YOGA_COURSE + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "dayofweek TEXT," +
                "start_time TEXT," +
                "end_time TEXT," +
                "capacity INTEGER," +
                "duration INTEGER," +
                "price REAL," +
                "type TEXT," +
                "description TEXT," +
                "start_date TEXT," +
                "end_date TEXT," +
                "created_at TEXT)";
        String CREATE_TABLE_YOGA_SCHEDULE = "CREATE TABLE " + TABLE_YOGA_SCHEDULE + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "course_id INTEGER," +
                "teacher_id INTEGER," +
                "date TEXT," +
                "comments TEXT," +
                "created_at TEXT," +
                "FOREIGN KEY(course_id) REFERENCES " + TABLE_YOGA_COURSE + "(_id) ON DELETE CASCADE," +
                "FOREIGN KEY(teacher_id) REFERENCES " + TABLE_TEACHERS + "(_id) ON DELETE CASCADE)";
        String CREATE_TABLE_TEACHERS = "CREATE TABLE " + TABLE_TEACHERS + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "email TEXT," +
                "profile_picture TEXT," +
                "created_at TEXT)";
        db.execSQL(CREATE_TABLE_YOGACOURSE);
        db.execSQL(CREATE_TABLE_YOGA_SCHEDULE);
        db.execSQL(CREATE_TABLE_TEACHERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA_COURSE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA_SCHEDULE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEACHERS);
            onCreate(db);
        }
    }
    // Yoga Course Code
    public long createNewYogaCourse(YogaCourse course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("dayofweek", course.getDayOfWeek());
        values.put("start_time", course.getStartTime());
        values.put("end_time", course.getEndTime());
        values.put("capacity", course.getCapacity());
        values.put("duration", course.getDuration());
        values.put("price", course.getPrice());
        values.put("type", course.getType());
        values.put("description", course.getDescription());
        values.put("start_date", DB_DATE_FORMAT.format(course.getStartDate()));
        values.put("end_date", DB_DATE_FORMAT.format(course.getEndDate()));
        values.put("created_at", DB_DATE_FORMAT.format(course.getCreatedAt()));

        long id = db.insert(TABLE_YOGA_COURSE, null, values);
        db.close();
        return id;
    }

    public List<YogaCourse> getAllYogaCourses() {
        List<YogaCourse> courses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                "_id", "dayofweek", "start_time", "end_time",
                "capacity", "duration", "price", "type",
                "description", "start_date", "end_date", "created_at"
        };

        Cursor cursor = db.query(TABLE_YOGA_COURSE, columns,
                null, null, null, null, "created_at DESC");

        try {
            if (cursor.moveToFirst()) {
                do {
                    YogaCourse course = new YogaCourse(
                            cursor.getInt(0), // id
                            cursor.getString(1), // dayOfWeek
                            cursor.getString(2), // startTime
                            cursor.getString(3), // endTime
                            cursor.getInt(4), // capacity
                            cursor.getInt(5), // duration
                            cursor.getFloat(6), // price
                            cursor.getString(7), // type
                            cursor.getString(8), // description
                            DB_DATE_FORMAT.parse(cursor.getString(9)), // startDate
                            DB_DATE_FORMAT.parse(cursor.getString(10)) // endDate
                    );
                    // createdAt is automatically set in YogaCourse constructor
                    courses.add(course);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error parsing dates", e);
        } finally {
            cursor.close();
            db.close();
        }
        return courses;
    }

    public YogaCourse getYogaCourse(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
                "_id", "dayofweek", "start_time", "end_time",
                "capacity", "duration", "price", "type",
                "description", "start_date", "end_date", "created_at"
        };

        Cursor cursor = db.query(TABLE_YOGA_COURSE, columns,
                "_id = ?", new String[]{String.valueOf(id)},
                null, null, null);

        try {
            if (cursor.moveToFirst()) {
                YogaCourse course = new YogaCourse(
                        cursor.getInt(0), // id
                        cursor.getString(1), // dayOfWeek
                        cursor.getString(2), // startTime
                        cursor.getString(3), // endTime
                        cursor.getInt(4), // capacity
                        cursor.getInt(5), // duration
                        cursor.getFloat(6), // price
                        cursor.getString(7), // type
                        cursor.getString(8), // description
                        DB_DATE_FORMAT.parse(cursor.getString(9)), // startDate
                        DB_DATE_FORMAT.parse(cursor.getString(10)) // endDate
                );
                // createdAt is automatically set in constructor
                return course;
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error parsing dates", e);
        } finally {
            cursor.close();
            db.close();
        }
        return null;
    }

    public int updateYogaCourse(YogaCourse course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("dayofweek", course.getDayOfWeek());
        values.put("start_time", course.getStartTime());
        values.put("end_time", course.getEndTime());
        values.put("capacity", course.getCapacity());
        values.put("duration", course.getDuration());
        values.put("price", course.getPrice());
        values.put("type", course.getType());
        values.put("description", course.getDescription());
        values.put("start_date", DB_DATE_FORMAT.format(course.getStartDate()));
        values.put("end_date", DB_DATE_FORMAT.format(course.getEndDate()));

        int rowsAffected = db.update(TABLE_YOGA_COURSE, values, "_id = ?",
                new String[]{String.valueOf(course.getId())});
        db.close();
        return rowsAffected;
    }

    public boolean deleteYogaCourse(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_YOGA_COURSE, "_id = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();  // Start transaction for atomic operations

            // Clear all tables
            db.delete(TABLE_YOGA_COURSE, null, null);
            db.delete(TABLE_YOGA_SCHEDULE, null, null);
            db.delete(TABLE_TEACHERS, null, null);

            // Reset auto-increment counters for all tables
            db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME IN (?, ?, ?)",
                    new String[]{TABLE_YOGA_COURSE, TABLE_YOGA_SCHEDULE, TABLE_TEACHERS});

            db.setTransactionSuccessful();  // Mark transaction as successful
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error clearing data", e);
        } finally {
            db.endTransaction();  // End transaction
            db.close();
        }
    }

    // Yoga Schedule Code
    public long createNewSchedule(YogaSchedule schedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("course_id", schedule.getCourseId());
        values.put("teacher_id", schedule.getTeacherId());
        values.put("date", DB_DATE_FORMAT.format(schedule.getDate()));
        values.put("comments", schedule.getComments());
        values.put("created_at", DB_DATE_FORMAT.format(schedule.getCreatedAt()));

        long id = db.insert(TABLE_YOGA_SCHEDULE, null, values);
        db.close();
        return id;
    }

    public List<YogaSchedule> getSchedulesForCourse(int courseId) {
        List<YogaSchedule> schedules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_YOGA_SCHEDULE,
                new String[]{"_id", "course_id", "teacher_id", "date", "comments", "created_at"},
                "course_id = ?", new String[]{String.valueOf(courseId)},
                null, null, "date ASC");

        try {
            if (cursor.moveToFirst()) {
                do {
                    YogaSchedule schedule = new YogaSchedule(
                            cursor.getInt(0),
                            cursor.getInt(1),
                            cursor.getInt(2), // teacher_id
                            DB_DATE_FORMAT.parse(cursor.getString(3)),
                            cursor.getString(4)
                    );
                    schedules.add(schedule);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error parsing dates", e);
        } finally {
            cursor.close();
            db.close();
        }
        return schedules;
    }

    public boolean deleteSchedule(long scheduleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_YOGA_SCHEDULE, "_id = ?",
                new String[]{String.valueOf(scheduleId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean updateSchedule(YogaSchedule schedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("date", DB_DATE_FORMAT.format(schedule.getDate()));
        values.put("teacher_id", schedule.getTeacherId());
        values.put("comments", schedule.getComments());

        int rowsAffected = db.update(TABLE_YOGA_SCHEDULE, values, "_id = ?",
                new String[]{String.valueOf(schedule.getId())});
        db.close();
        return rowsAffected > 0;
    }

    public YogaSchedule getSchedule(int scheduleId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_YOGA_SCHEDULE,
                new String[]{"_id", "course_id", "teacher_id", "date", "comments", "created_at"},
                "_id = ?", new String[]{String.valueOf(scheduleId)},
                null, null, null);

        try {
            if (cursor.moveToFirst()) {
                return new YogaSchedule(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        DB_DATE_FORMAT.parse(cursor.getString(3)),
                        cursor.getString(4)
                );
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error parsing dates", e);
        } finally {
            cursor.close();
            db.close();
        }
        return null;
    }

    public List<YogaSchedule> getAllSchedules() {
        List<YogaSchedule> schedules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_YOGA_SCHEDULE,
                new String[]{"_id", "course_id", "teacher_id", "date", "comments", "created_at"},
                null, null, null, null, "date ASC");

        try {
            if (cursor.moveToFirst()) {
                do {
                    YogaSchedule schedule = new YogaSchedule(
                            cursor.getInt(0),
                            cursor.getInt(1),
                            cursor.getInt(2),
                            DB_DATE_FORMAT.parse(cursor.getString(3)),
                            cursor.getString(4)
                    );
                    schedules.add(schedule);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error parsing dates", e);
        } finally {
            cursor.close();
            db.close();
        }
        return schedules;
    }

    // Teachers Code
    public long createTeacher(Teacher teacher) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", teacher.getName());
        values.put("email", teacher.getEmail());
        values.put("profile_picture", teacher.getProfilePicturePath());
        values.put("created_at", DB_DATE_FORMAT.format(teacher.getCreatedAt()));

        long id = db.insert(TABLE_TEACHERS, null, values);
        db.close();
        return id;
    }

    public List<Teacher> getAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TEACHERS,
                new String[]{"_id", "name", "email", "profile_picture", "created_at"},
                null, null, null, null, "name ASC");

        if (cursor.moveToFirst()) {
            do {
                Teacher teacher = new Teacher(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                );
                teachers.add(teacher);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return teachers;
    }

    public Teacher getTeacher(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
                "_id", "name", "email", "profile_picture"
        };

        Cursor cursor = db.query(TABLE_TEACHERS, columns,
                "_id = ?", new String[]{String.valueOf(id)},
                null, null, null);

        try {
            if (cursor.moveToFirst()) {
                return new Teacher(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                );
            }
        } finally {
            cursor.close();
            db.close();
        }
        return null;
    }

    public int updateTeacher(Teacher teacher) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", teacher.getName());
        values.put("email", teacher.getEmail());
        values.put("profile_picture", teacher.getProfilePicturePath());

        int rowsAffected = db.update(TABLE_TEACHERS, values, "_id = ?",
                new String[]{String.valueOf(teacher.getId())});
        db.close();
        return rowsAffected;
    }

    public boolean deleteTeacher(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_TEACHERS, "_id = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }

    @Override
    public void close() {
        super.close();
    }

    public static SimpleDateFormat getDbDateFormat() {
        return DB_DATE_FORMAT;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);  // Enable foreign key constraints
    }
}