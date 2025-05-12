package com.example.schedule_application.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.schedule_application.R;
import com.example.schedule_application.model.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView taskNameTextView, taskDescriptionTextView, taskCategoryTextView;
    private TextView taskTimeTextView, taskDurationTextView;
    private Button startTimerButton, editTaskButton, deleteTaskButton;
    private FirebaseFirestore db;
    private FirebaseUser user;


    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        initViews();

        currentTask = (Task) getIntent().getSerializableExtra("tasks");

        if (currentTask == null) {
            Toast.makeText(this, "Task data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        displayTaskData();
        setupButtonListeners();
    }

    private void initViews() {
        taskNameTextView = findViewById(R.id.taskNameTextView);
        taskDescriptionTextView = findViewById(R.id.taskDescriptionTextView);
        taskCategoryTextView = findViewById(R.id.taskCategoryTextView);
        taskTimeTextView = findViewById(R.id.taskTimeTextView);
        taskDurationTextView = findViewById(R.id.taskDurationTextView);

        startTimerButton = findViewById(R.id.startTimerButton);
        editTaskButton = findViewById(R.id.editTaskButton);
        deleteTaskButton = findViewById(R.id.deleteTaskButton);
    }

    private void displayTaskData() {
        taskNameTextView.setText(currentTask.getName());

        String description = (currentTask.getDescription() == null || currentTask.getDescription().trim().isEmpty())
                ? "No description provided."
                : currentTask.getDescription();
        taskDescriptionTextView.setText(description);

        String category = (currentTask.getCategory() == null || currentTask.getCategory().trim().isEmpty())
                ? "Uncategorized"
                : currentTask.getCategory();
        taskCategoryTextView.setText(category);

        taskTimeTextView.setText(currentTask.getTime());
        taskDurationTextView.setText(currentTask.getDuration() + " mins");
    }

    private void setupButtonListeners() {

        startTimerButton.setOnClickListener(v -> {
            Toast.makeText(this, "Starting countdown timer", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, CountdownTimerActivity.class);
            intent.putExtra("tasks", currentTask);
            startActivity(intent);
        });

        editTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra("taskId", currentTask.getId());
            startActivity(intent);
        });

        deleteTaskButton.setOnClickListener(v -> {
            db.collection("users").document(user.getUid())
                    .collection("tasks").whereEqualTo("id", currentTask.getId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            doc.getReference().delete();
                        }

                        Toast.makeText(TaskDetailActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(TaskDetailActivity.this, DashboardActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();  // Finish current activity so it closes
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(TaskDetailActivity.this, "Failed to delete task", Toast.LENGTH_SHORT).show()
                    );
        });

    }
}

