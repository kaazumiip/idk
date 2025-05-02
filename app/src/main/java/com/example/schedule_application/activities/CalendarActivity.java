package com.example.schedule_application.activities;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.schedule_application.R;
import com.example.schedule_application.model.Task;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView tvSelectedDate;
    private LinearLayout taskListLayout;
    private List<Task> tasksList = new ArrayList<>();

    // Firebase instance for fetching tasks (assuming you're using Firestore)
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        taskListLayout = findViewById(R.id.taskListLayout);

        // Fetch tasks from Firestore
        fetchTasksFromFirestore();

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            String selectedDate = date.getDay() + "/" + (date.getMonth() + 1) + "/" + date.getYear();
            tvSelectedDate.setText("Selected date: " + selectedDate);

            // Show tasks for the selected date
            showTasksForSelectedDate(date.getYear(), date.getMonth(), date.getDay());
        });
    }

    private void fetchTasksFromFirestore() {
        // Fetch tasks from Firestore (example with Firebase Firestore)
        db.collection("tasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tasksList.clear();
                    List<Task> fetchedTasks = queryDocumentSnapshots.toObjects(Task.class);
                    tasksList.addAll(fetchedTasks);
                })
                .addOnFailureListener(e -> {
                    // Handle failure if any
                });
    }

    private void showTasksForSelectedDate(int year, int month, int day) {
        // Clear previous tasks from layout
        taskListLayout.removeAllViews();

        // Iterate over the tasks and check if they match the selected date
        for (Task task : tasksList) {
            // Check if task date matches the selected date
            if (isSameDay(task.getTimestamp().toDate(), year, month, day)) {
                // Create a TextView for each task and add it to the layout
                TextView taskView = new TextView(this);
                taskView.setText(task.getName());  // You can customize this view as needed
                taskListLayout.addView(taskView);
            }
        }
    }

    // Helper function to check if the task's date matches the selected date
    private boolean isSameDay(Date taskDate, int year, int month, int day) {
        // Check if the task's timestamp is on the same day as the selected date
        android.icu.util.Calendar calendar = android.icu.util.Calendar.getInstance();
        calendar.setTime(taskDate);

        return calendar.get(android.icu.util.Calendar.YEAR) == year &&
                calendar.get(android.icu.util.Calendar.MONTH) == month &&
                calendar.get(android.icu.util.Calendar.DAY_OF_MONTH) == day;
    }
}
