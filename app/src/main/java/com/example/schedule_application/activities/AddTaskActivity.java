package com.example.schedule_application.activities;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ParseException;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AddTaskActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private EditText nameEditText, descriptionEditText, durationEditText;
    private EditText dateEditText, timeEditText;
    private Spinner categorySpinner;
    private Button saveButton;
    private Button clearButton;
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
        clearButton = findViewById(R.id.btnClear);
        clearButton.setOnClickListener(v -> {
            Intent fakeIntent = new Intent(this, TaskReminderReceiver.class);
            fakeIntent.putExtra("taskId", "FAKE_TASK_TEST");

            sendBroadcast(fakeIntent);
        });


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
        task.setStatus("not complete");

        firestore.collection("users")
                .document(user.getUid())
                .collection("tasks")
                .document(task.getId())  // use YOUR generated id here
                .set(task)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show();
                    long millis = task.getTimestamp().toDate().getTime();
                    scheduleTaskReminders(millis, task.getId());
                    Log.d("lwiay", "calendar : " +millis);

                    clearInputs();
                    fetchTasks();
                })
                .addOnFailureListener(e -> {
                    Log.e("AddTaskActivity", "Error saving task: " + e.getMessage());
                });

    }


    private void scheduleTaskReminders(long taskTimestamp, String taskId) {
        int[] reminderTimes = {30, 15, 5, 0};

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Toast.makeText(this, "AlarmManager unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Enable exact alarm permission in settings", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (int minutesBefore : reminderTimes) {
            long reminderTimestamp = taskTimestamp - (minutesBefore * 60 * 1000);
            long currentTime = System.currentTimeMillis();

            Log.d("ReminderTime", "Reminder for: " + sdf.format(new Date(reminderTimestamp)));
            Log.d("CurrentTime", "Current time: " + sdf.format(new Date(currentTime)));

            if (reminderTimestamp > currentTime) {
                Log.d("compare time", "Setting alarm for: " + sdf.format(new Date(reminderTimestamp)));

                Intent reminderIntent = new Intent(this, TaskReminderReceiver.class);
                reminderIntent.putExtra("taskId", taskId);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        (taskId + minutesBefore).hashCode(),
                        reminderIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                try {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimestamp, pendingIntent);
                } catch (SecurityException e) {
                    Toast.makeText(this, "Permission denied for exact alarm", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d("compare time", "Skipped past reminder for: " + sdf.format(new Date(reminderTimestamp)));
            }
        }
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
    }

    private void deleteTask(String taskId) {
        if (user == null) return;

        firestore.collection("users")
                .document(user.getUid())
                .collection("tasks")
                .whereEqualTo("id" , taskId) .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                        Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                    }

                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete task", Toast.LENGTH_SHORT).show());




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
