package com.example.schedule_application.activities;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.schedule_application.Adapter.TaskAdapter;
import com.example.schedule_application.R;
import com.example.schedule_application.model.Task;
import com.example.schedule_application.receivers.TaskReminderReceiver;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private EditText nameEditText, descriptionEditText, durationEditText;
    private EditText dateEditText, timeEditText;
    private Spinner categorySpinner;
    private Button saveButton;
    private RecyclerView recyclerView;

    private FirebaseFirestore firestore;
    private FirebaseUser user;
    private Calendar selectedCalendar;
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        nameEditText = findViewById(R.id.taskNameEditText);
        descriptionEditText = findViewById(R.id.taskDescriptionEditText);
        categorySpinner = findViewById(R.id.taskCategorySpinner);
        durationEditText = findViewById(R.id.taskDurationEditText);
        dateEditText = findViewById(R.id.taskDateEditText);
        timeEditText = findViewById(R.id.taskTimeEditText);
        saveButton = findViewById(R.id.saveTaskButton);
        recyclerView = findViewById(R.id.tasksRecyclerView);

        firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        selectedCalendar = Calendar.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(taskAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item, // for selected item view
                new String[]{"Work", "Study", "Exercise", "Relaxation", "Health"}
        );
        categoryAdapter.setDropDownViewResource(R.layout.spinner_item);
        categorySpinner.setAdapter(categoryAdapter);



        dateEditText.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedCalendar.set(Calendar.YEAR, year);
                        selectedCalendar.set(Calendar.MONTH, month);
                        selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.getTime());
                        dateEditText.setText(formattedDate);
                    },
                    selectedCalendar.get(Calendar.YEAR),
                    selectedCalendar.get(Calendar.MONTH),
                    selectedCalendar.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        timeEditText.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedCalendar.set(Calendar.MINUTE, minute);
                        String formattedTime = String.format("%02d:%02d", hourOfDay, minute);
                        timeEditText.setText(formattedTime);
                    },
                    selectedCalendar.get(Calendar.HOUR_OF_DAY),
                    selectedCalendar.get(Calendar.MINUTE),
                    true
            );
            timePicker.show();
        });

        saveButton.setOnClickListener(v -> saveTask());
        fetchTasks();
    }

    private void saveTask() {
        String name = nameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString().trim();
        String time = timeEditText.getText().toString().trim();
        String duration = durationEditText.getText().toString().trim();

        if (name.isEmpty() || time.isEmpty() || duration.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user == null) return;

        // Create the task without the need for reminder parameters
        Task task = new Task(
                name,
                description,
                time,
                duration,
                category,
                new Timestamp(selectedCalendar.getTime()),
                false
        );

        firestore.collection("users")
                .document(user.getUid())
                .collection("tasks")
                .add(task)
                .addOnSuccessListener(documentReference -> {
                    String generatedId = documentReference.getId();
                    task.setId(generatedId);

                    documentReference.update("id", generatedId)
                            .addOnSuccessListener(aVoid -> Log.d("AddTaskActivity", "Task ID updated successfully"));

                    Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show();
                    scheduleTaskReminders(selectedCalendar, generatedId);

                    clearInputs();
                    fetchTasks();

                    // No need to schedule reminders here, the reminders are calculated and stored in the Task itself
                });
    }


    private void scheduleTaskReminders(Calendar taskCalendar, String taskId) {
        int[] reminderTimes = {30, 15, 5}; // Reminder times in minutes before the task
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Set reminders for 30, 15, and 5 minutes before the task time
        for (int minutesBefore : reminderTimes) {
            Calendar reminderTime = (Calendar) taskCalendar.clone();
            reminderTime.add(Calendar.MINUTE, -minutesBefore);
            Log.d("Reminder", "Reminder time (" + minutesBefore + " minutes before): " + reminderTime.getTime());


            Intent reminderIntent = new Intent(this, TaskReminderReceiver.class);
            reminderIntent.putExtra("taskId", taskId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);
        }

        // Set a reminder exactly at the task time
        Intent onTimeReminderIntent = new Intent(this, TaskReminderReceiver.class);
        onTimeReminderIntent.putExtra("taskId", taskId);

        PendingIntent onTimePendingIntent = PendingIntent.getBroadcast(this, 0, onTimeReminderIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, taskCalendar.getTimeInMillis(), onTimePendingIntent);
    }


    private void fetchTasks() {
        if (user == null) return;

        firestore.collection("users")
                .document(user.getUid())
                .collection("tasks")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        Toast.makeText(this, "Failed to fetch tasks", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Task> taskList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        Task task = doc.toObject(Task.class);
                        if (task != null) taskList.add(task);
                    }

                    taskAdapter.updateTaskList(taskList);
                });
    }

    @Override
    public void onTaskUpdate(String taskId) {
        Intent intent = new Intent(AddTaskActivity.this, EditActivity.class);
        intent.putExtra("taskId", taskId);
        startActivity(intent);
    }

    @Override
    public void onTaskDelete(String taskId) {
        deleteTask(taskId);
    }
    @Override
    public void onTaskClick(Task task) {
        // do s.th when a task is clicked
    }

    private void deleteTask(String taskId) {
        if (user == null) return;

        firestore.collection("users")
                .document(user.getUid())
                .collection("tasks")
                .document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                    taskAdapter.removeTaskById(taskId);  // Remove from RecyclerView
                })
                .addOnFailureListener(e -> {
                    Log.e("TaskDelete", "Error deleting task: " + e.getMessage());
                    Toast.makeText(this, "Error deleting task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearInputs() {
        nameEditText.setText("");
        descriptionEditText.setText("");
        categorySpinner.setSelection(0);
        durationEditText.setText("");
        dateEditText.setText("");
        timeEditText.setText("");
        selectedCalendar = Calendar.getInstance();
    }
}
