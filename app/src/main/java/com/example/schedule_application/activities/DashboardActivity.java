package com.example.schedule_application.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.schedule_application.Adapter.TaskAdapter;
import com.example.schedule_application.R;
import com.example.schedule_application.model.Task;
import com.example.schedule_application.utils.GitHubAIClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private TextView recommendationTextView, welcomeTextView;
    private List<Task> allTasks = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser user;
    private ProgressBar progressBar;
    private Button recommendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        // Initialize views
        recyclerView = findViewById(R.id.rvTasks);
        recommendationTextView = findViewById(R.id.recommendationText);
        welcomeTextView = findViewById(R.id.tvWelcome);
        Button btnAddTask = findViewById(R.id.btnAddTask);
        Button btnCalendar = findViewById(R.id.btnCalendar);
        Button btnProfile = findViewById(R.id.btnProfile);
        progressBar = findViewById(R.id.progressBar);
        recommendBtn = findViewById(R.id.recommendBtn);

        // Placeholder recommendation text
        recommendationTextView.setText("Tap the button to get your recommendations.");

        // Initialize Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(this, allTasks, new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskUpdate(String taskId) {
                // TODO: Handle task update
            }
            @Override
            public void onTaskClick(Task task) {
                if (task != null) {
                    Log.d("dashtask", "Task clicked: " + task.getName());
                    Intent intent = new Intent(DashboardActivity.this, CountdownTimerActivity.class);
                    intent.putExtra("tasks", task);
                    startActivity(intent);
                } else {
                    Log.d("dashtask", "Task is null, cannot pass to intent");
                }
            }



            @Override
            public void onTaskDelete(String taskId) {
                db.collection("users").document(user.getUid())
                        .collection("tasks").document(taskId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(DashboardActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                            fetchTasksFromFirebase();
                        })
                        .addOnFailureListener(e -> Toast.makeText(DashboardActivity.this, "Failed to delete task", Toast.LENGTH_SHORT).show());
            }

        });
        recyclerView.setAdapter(taskAdapter);

        // Set up navigation buttons
        btnAddTask.setOnClickListener(v -> startActivity(new Intent(this, AddTaskActivity.class)));
        btnCalendar.setOnClickListener(v -> startActivity(new Intent(this, CalendarActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileView.class)));

        recommendBtn.setOnClickListener(v -> {
            if (allTasks.isEmpty()) {
                recommendationTextView.setText("Add some tasks to get personalized recommendations!");
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            requestRecommendationFromGitHubAI();
        });



        // Fetch name and tasks
        fetchUserName();
        fetchTasksFromFirebase();
    }

    private void fetchUserName() {
        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("username");
                        welcomeTextView.setText("Welcome, " + (name != null ? name : "User"));
                    } else {
                        welcomeTextView.setText("Welcome!");
                    }
                })
                .addOnFailureListener(e -> welcomeTextView.setText("Welcome!"));
    }

    private void fetchTasksFromFirebase() {
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .collection("tasks")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        Toast.makeText(this, "Failed to fetch tasks", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    allTasks.clear();  // clear before adding fresh
                    List<Task> tasks = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        Task task = doc.toObject(Task.class);
                        if (task != null) tasks.add(task);
                    }
                    Log.d("Dashboard", "Tasks after fetch: " + tasks.size());


                    allTasks.addAll(tasks);  // store all tasks here
                    taskAdapter.updateTaskList(tasks);
                });
    }

    private void requestRecommendationFromGitHubAI() {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Based on these tasks, what are some recommendations to prioritize or improve productivity?\n");
        for (Task task : allTasks) {
            promptBuilder.append("- ").append(task.getName());
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                promptBuilder.append(": ").append(task.getDescription());
            }
            promptBuilder.append("\n");
        }

        String prompt = promptBuilder.toString();

        GitHubAIClient.getRecommendation(prompt, new GitHubAIClient.GitHubCallback() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    recommendationTextView.setText(result);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(DashboardActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    recommendationTextView.setText("Failed to get recommendation. Please try again.");
                });
            }
        });
    }

}
