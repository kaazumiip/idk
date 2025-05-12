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
import com.example.schedule_application.model.Recommendations;
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
    private static final String TAG = "DashboardActivity";

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private TextView recommendationTextView, welcomeTextView;
    private List<Task> allTasks = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser user;
    private ProgressBar progressBar;
    private Button recommendBtn;
    private Button checkModelsBtn; // Button for checking models

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

        // Initialize the check models button if it exists in layout
        try {
            checkModelsBtn.setOnClickListener(v -> checkAvailableDeepSeekModels());
        } catch (Exception e) {
            Log.w(TAG, "Check models button not found in layout", e);
        }

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
            requestRecommendation();
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

    private void requestRecommendation() {
        progressBar.setVisibility(View.VISIBLE);

        StringBuilder promptBuilder = new StringBuilder("Based on these tasks, what are some recommendations to prioritize or improve productivity?\n");
        for (Task task : allTasks) {
            promptBuilder.append("- ").append(task.getName());
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                promptBuilder.append(": ").append(task.getDescription());
            }
            promptBuilder.append("\n");
        }

        String prompt = promptBuilder.toString();
        Log.d(TAG, "Sending prompt to OpenRouter: " + prompt);

        // Try the API first
        DeepSeekApiService apiService = new DeepSeekApiService();
        apiService.getRecommendations(prompt, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "OpenRouter API call failed, falling back to local recommendation", e);
                provideLocalRecommendation();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String resultJson = response.body().string();
                    Log.d(TAG, "OpenRouter API Response: " + resultJson);

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
                        Log.e(TAG, "Failed to parse OpenRouter response, falling back to local recommendation", e);
                        provideLocalRecommendation();
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    Log.e(TAG, "OpenRouter API error response: " + errorBody);
                    provideLocalRecommendation();
                }
            }
        });
    }

    // Method to provide local recommendations as fallback
    private void provideLocalRecommendation() {
        runOnUiThread(() -> {
            try {
                String localRecommendation = Recommendations.generate(allTasks);
                progressBar.setVisibility(View.GONE);
                recommendationTextView.setText(localRecommendation);
                Log.i(TAG, "Used local recommendation: " + localRecommendation);
            } catch (Exception e) {
                progressBar.setVisibility(View.GONE);
                recommendationTextView.setText("Could not generate recommendation. Please try again later.");
                Log.e(TAG, "Error generating local recommendation", e);
            }
        });
    }

    // Method to discover available models on OpenRouter
    private void checkAvailableDeepSeekModels() {
        Toast.makeText(this, "Checking available models...", Toast.LENGTH_SHORT).show();

        DeepSeekApiService apiService = new DeepSeekApiService();
        apiService.listAvailableModels(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to get available models", e);
                runOnUiThread(() -> {
                    Toast.makeText(DashboardActivity.this,
                            "Failed to get models: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "No response body";

                if (response.isSuccessful()) {
                    Log.d(TAG, "Available OpenRouter models: " + responseBody);

                    // Try to extract model names for easier reading
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray data = jsonResponse.getJSONArray("data");

                        StringBuilder modelNames = new StringBuilder("Available models:\n");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject model = data.getJSONObject(i);
                            modelNames.append("- ").append(model.getString("id")).append("\n");
                        }

                        String modelList = modelNames.toString();
                        Log.i(TAG, modelList);

                        // Show in UI for debugging
                        runOnUiThread(() -> {
                            Toast.makeText(DashboardActivity.this,
                                    "Models found! Check logs for details",
                                    Toast.LENGTH_SHORT).show();
                            // Temporarily display in recommendation text
                            recommendationTextView.setText(modelList);
                        });
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing models response", e);
                        runOnUiThread(() -> {
                            Toast.makeText(DashboardActivity.this,
                                    "Error parsing models: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    Log.e(TAG, "Error getting models: " + responseBody);
                    runOnUiThread(() -> {
                        Toast.makeText(DashboardActivity.this,
                                "Error getting models: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}