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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_application.Adapter.TaskAdapter;
import com.example.schedule_application.R;
import com.example.schedule_application.model.Task;
import com.example.schedule_application.utils.DeepSeekApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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

        recyclerView = findViewById(R.id.rvTasks);
        recommendationTextView = findViewById(R.id.recommendationText);
        welcomeTextView = findViewById(R.id.tvWelcome);
        Button btnAddTask = findViewById(R.id.btnAddTask);
        Button btnCalendar = findViewById(R.id.btnCalendar);
        Button btnProfile = findViewById(R.id.btnProfile);
        progressBar = findViewById(R.id.progressBar);
        recommendBtn = findViewById(R.id.recommendBtn);

        recommendationTextView.setText("Tap the button to get your recommendations.");

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(this, allTasks, new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskUpdate(String taskId) {}

            @Override
            public void onTaskClick(Task task) {
                if (task != null) {
                    Log.d("dashtask", "Task clicked: " + task.getName());
                    Intent intent = new Intent(DashboardActivity.this, TaskDetailActivity.class);
                    intent.putExtra("tasks", task);
                    startActivity(intent);
                }
            }

            @Override
            public void onTaskDelete(String taskId) {
                db.collection("users").document(user.getUid())
                        .collection("tasks").whereEqualTo("id", taskId)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                doc.getReference().delete();
                                Toast.makeText(DashboardActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                            }
                            fetchTasksFromFirebase();
                        })
                        .addOnFailureListener(e -> Toast.makeText(DashboardActivity.this, "Failed to delete task", Toast.LENGTH_SHORT).show());
            }
        });
        recyclerView.setAdapter(taskAdapter);

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

        fetchUserName();
        fetchTasksFromFirebase();
    }

    private void fetchUserName() {
        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String name = documentSnapshot.getString("username");
                    welcomeTextView.setText("Welcome, " + (name != null ? name : "User"));
                })
                .addOnFailureListener(e -> welcomeTextView.setText("Welcome!"));
    }

    private void fetchTasksFromFirebase() {
        db.collection("users")
                .document(user.getUid())
                .collection("tasks")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        Toast.makeText(this, "Failed to fetch tasks", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    allTasks.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        Task task = doc.toObject(Task.class);
                        if (task != null) {
                            allTasks.add(task);
                        }
                    }
                    taskAdapter.updateTaskList(allTasks);
                });
    }

    private void requestRecommendationFromGitHubAI() {
        StringBuilder promptBuilder = new StringBuilder("Based on these tasks, what are some recommendations to prioritize or improve productivity?\n");
        for (Task task : allTasks) {
            promptBuilder.append("- ").append(task.getName());
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                promptBuilder.append(": ").append(task.getDescription());
            }
            promptBuilder.append("\n");
        }

        String prompt = promptBuilder.toString();
        DeepSeekApiService apiService = new DeepSeekApiService();
        apiService.getRecommendations(prompt, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    recommendationTextView.setText("Failed to get recommendation. Please try again.");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String resultJson = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(resultJson);
                        JSONArray choices = jsonObject.getJSONArray("choices");
                        String content = choices.getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            recommendationTextView.setText(content.trim());
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            recommendationTextView.setText("Failed to parse recommendation.");
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        recommendationTextView.setText("Recommendation failed.");
                    });
                }
            }
        });
    }
}
