package com.example.schedule_application.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.schedule_application.R;
import com.example.schedule_application.model.Task;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView taskNameTextView, taskDescriptionTextView, taskCategoryTextView;
    private TextView taskTimeTextView, taskDurationTextView;
    private Button startTimerButton, editTaskButton, deleteTaskButton;
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Initialize views
        initViews();

        // Get task data from intent
        currentTask = (Task) getIntent().getSerializableExtra("task");

        if (currentTask == null) {
            Toast.makeText(this, "Objective data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate UI with task data
        displayTaskData();

        // Set up button listeners
        setupButtonListeners();
    }

    private void initViews() {
        // Text views
        taskNameTextView = findViewById(R.id.taskNameTextView);
        taskDescriptionTextView = findViewById(R.id.taskDescriptionTextView);
        taskCategoryTextView = findViewById(R.id.taskCategoryTextView);
        taskTimeTextView = findViewById(R.id.taskTimeTextView);
        taskDurationTextView = findViewById(R.id.taskDurationTextView);

        // Buttons
        startTimerButton = findViewById(R.id.startTimerButton);
        editTaskButton = findViewById(R.id.editTaskButton);
        deleteTaskButton = findViewById(R.id.deleteTaskButton);
    }

    private void displayTaskData() {
        // Set text for all fields
        taskNameTextView.setText(currentTask.getName());

        // Handle description - provide default if empty
        String description = currentTask.getDescription();
        if (description == null || description.trim().isEmpty()) {
            description = "No description provided for this objective.";
        }
        taskDescriptionTextView.setText(description);

        // Set category with default if needed
        String category = currentTask.getCategory();
        if (category == null || category.trim().isEmpty()) {
            category = "Uncategorized";
        }
        taskCategoryTextView.setText(category);

        // Set time and duration
        taskTimeTextView.setText(currentTask.getTime());
        taskDurationTextView.setText(currentTask.getDuration() + " mins");
    }

    private void setupButtonListeners() {
        // Start timer button listener
        startTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show futuristic toast
                Toast.makeText(TaskDetailActivity.this,
                        "INITIATING COUNTDOWN SEQUENCE", Toast.LENGTH_SHORT).show();

                // Launch countdown activity
                Intent intent = new Intent(TaskDetailActivity.this, CountdownTimerActivity.class);
                intent.putExtra("task", currentTask);
                startActivity(intent);
            }
        });

        // Edit button listener
        editTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement edit functionality
                Toast.makeText(TaskDetailActivity.this,
                        "EDIT FUNCTION NOT IMPLEMENTED", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete button listener
        deleteTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement delete functionality
                Toast.makeText(TaskDetailActivity.this,
                        "DELETE FUNCTION NOT IMPLEMENTED", Toast.LENGTH_SHORT).show();
            }
        });
    }
}