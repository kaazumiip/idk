package com.example.schedule_application.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.schedule_application.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditActivity extends AppCompatActivity {

    private EditText taskNameEditText, taskDescriptionEditText, taskDurationEditText, taskDateEditText, taskTimeEditText;
    private Spinner taskCategorySpinner, taskStatusSpinner;
    private SeekBar completionSeekBar;
    private TextView completionPercentageText, taskIdTextView, lastCompletedTextView;
    private Button updateTaskButton;

    private FirebaseFirestore db;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        db = FirebaseFirestore.getInstance();

        taskId = getIntent().getStringExtra("taskId");

        taskNameEditText = findViewById(R.id.taskNameEditText);
        taskDescriptionEditText = findViewById(R.id.taskDescriptionEditText);
        taskDurationEditText = findViewById(R.id.taskDurationEditText);
        taskDateEditText = findViewById(R.id.taskDateEditText);
        taskTimeEditText = findViewById(R.id.taskTimeEditText);
        taskCategorySpinner = findViewById(R.id.taskCategorySpinner);
        taskStatusSpinner = findViewById(R.id.taskStatusSpinner);
        completionSeekBar = findViewById(R.id.completionSeekBar);
        completionPercentageText = findViewById(R.id.completionPercentageText);
        taskIdTextView = findViewById(R.id.taskIdTextView);
        lastCompletedTextView = findViewById(R.id.lastCompletedTextView);
        updateTaskButton = findViewById(R.id.updateTaskButton);


        taskIdTextView.setText("TASK ID: #" + taskId);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                new String[]{"Work", "Study", "Exercise", "Relaxation", "Health"}
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskCategorySpinner.setAdapter(categoryAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                new String[]{"Not Started", "In Progress", "Completed"}
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskStatusSpinner.setAdapter(statusAdapter);

        // Setup SeekBar sync
        completionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                completionPercentageText.setText(progress + "%");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Setup date and time pickers
        taskDateEditText.setOnClickListener(v -> showDatePicker());
        taskTimeEditText.setOnClickListener(v -> showTimePicker());

        // Load task data
        loadTaskData();

        // Update
        updateTaskButton.setOnClickListener(v -> updateTask());


    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%02d", dayOfMonth, month + 1, year % 100);
                    taskDateEditText.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    taskTimeEditText.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        dialog.show();
    }

    private void loadTaskData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        db.collection("users")
                .document(userId)
                .collection("tasks")
                .document(taskId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        taskNameEditText.setText(snapshot.getString("name"));
                        taskDescriptionEditText.setText(snapshot.getString("description"));
                        taskDurationEditText.setText(snapshot.getString("duration"));
                        taskDateEditText.setText(snapshot.getString("date"));
                        taskTimeEditText.setText(snapshot.getString("time"));

                        Long completion = snapshot.getLong("completion");
                        if (completion != null) {
                            completionSeekBar.setProgress(completion.intValue());
                            completionPercentageText.setText(completion.intValue() + "%");
                        }

                        ArrayAdapter<String> categoryAdapter = (ArrayAdapter<String>) taskCategorySpinner.getAdapter();
                        ArrayAdapter<String> statusAdapter = (ArrayAdapter<String>) taskStatusSpinner.getAdapter();
                        String category = snapshot.getString("category");
                        String status = snapshot.getString("status");

                        if (category != null && categoryAdapter != null)
                            taskCategorySpinner.setSelection(categoryAdapter.getPosition(category));
                        if (status != null && statusAdapter != null)
                            taskStatusSpinner.setSelection(statusAdapter.getPosition(status));

                        lastCompletedTextView.setText("COMPLETED: " + snapshot.getString("completedAt"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EditActivity", "Failed to load task", e);
                    Toast.makeText(this, "Error loading task", Toast.LENGTH_SHORT).show();
                });
    }


    private void updateTask() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        if (taskId == null || taskId.isEmpty()) {
            Toast.makeText(this, "Task ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> task = new HashMap<>();
        task.put("name", taskNameEditText.getText().toString());
        task.put("description", taskDescriptionEditText.getText().toString());
        task.put("duration", taskDurationEditText.getText().toString());
        task.put("date", taskDateEditText.getText().toString());
        task.put("time", taskTimeEditText.getText().toString());
        task.put("category", taskCategorySpinner.getSelectedItem().toString());
        task.put("status", taskStatusSpinner.getSelectedItem().toString());
        task.put("completion", completionSeekBar.getProgress());
        task.put("modifiedAt", Timestamp.now());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("tasks")
                .document(taskId)
                .update(task)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Task updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}





