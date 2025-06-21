//package com.example.schedule_application.activities;
//
//import android.graphics.Color;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Button;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.schedule_application.R;
//import com.example.schedule_application.Adapter.RecentActivityAdapter;
//import com.example.schedule_application.model.ActivityLog;
//import com.github.mikephil.charting.charts.PieChart;
//import com.github.mikephil.charting.components.Legend;
//import com.github.mikephil.charting.data.PieData;
//import com.github.mikephil.charting.data.PieDataSet;
//import com.github.mikephil.charting.data.PieEntry;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.Query;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.HashMap;
//
//public class ProfileView extends AppCompatActivity {
//    private TextView usernameTextView, emailTextView, progressPercentageTextView;
//    private TextView completedTasksCount, pendingTasksCount, totalTasksCount;
//    private ProgressBar taskCompletionProgress;
//    private PieChart taskStatusChart;
//    private Button btnEditProfile, btnViewAllTasks;
//    private RecyclerView recentActivityRecyclerView;
//    private RecentActivityAdapter activityAdapter;
//    private ArrayList<ActivityLog> activityLogList;
//    private FirebaseFirestore db;
//    private String uid;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_profile_view);
//
//        db = FirebaseFirestore.getInstance();
//        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        initializeViews();
//        setupRecyclerView();
//        loadUserProfile();
//        loadTaskStatistics();
//
//
//        listenToRecentActivities();
//        setupButtonListeners();
//    }
//
//    private void initializeViews() {
//        usernameTextView = findViewById(R.id.textUsername);
//        emailTextView = findViewById(R.id.textEmail);
//
//        completedTasksCount = findViewById(R.id.completedTasksCount);
//        pendingTasksCount = findViewById(R.id.pendingTasksCount);
//        totalTasksCount = findViewById(R.id.totalTasksCount);
//        taskCompletionProgress = findViewById(R.id.taskCompletionProgress);
//        progressPercentageTextView = findViewById(R.id.progressPercentage);
//
//        taskStatusChart = findViewById(R.id.taskStatusChart);
//        setupEmptyChart();
//
//        recentActivityRecyclerView = findViewById(R.id.recentActivityRecyclerView);
//
//        btnEditProfile = findViewById(R.id.btnEditProfile);
//        btnViewAllTasks = findViewById(R.id.btnViewAllTasks);
//    }
//
//    private void setupRecyclerView() {
//        activityLogList = new ArrayList<>();
//        activityAdapter = new RecentActivityAdapter(activityLogList);
//        recentActivityRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recentActivityRecyclerView.setAdapter(activityAdapter);
//    }
//
//    private void loadUserProfile() {
//        db.collection("users")
//                .document(uid)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        String username = documentSnapshot.getString("username");
//                        String email = documentSnapshot.getString("email");
//
//                        usernameTextView.setText(username);
//                        emailTextView.setText(email);
//                    } else {
//                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//    }
//
//    private void loadTaskStatistics() {
//        taskCompletionProgress.setVisibility(ProgressBar.VISIBLE);
//
//        db.collection("users")
//                .document(uid)
//                .collection("tasks")
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        int completed = 0;
//                        int pending = 0;
//
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            String status = document.getString("status");
//                            if ("Completed".equalsIgnoreCase(status)) {
//                                completed++;
//                            } else {
//                                pending++;
//                            }
//                        }
//
//                        updateTaskStatistics(completed, pending);
//                    } else {
//                        Toast.makeText(this, "Error loading tasks: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    private void listenToRecentActivities() {
//        db.collection("users")
//                .document(uid)
//                .collection("activities")
//                .orderBy("timestamp", Query.Direction.DESCENDING)
//                .limit(10)
//                .addSnapshotListener((snapshots, e) -> {
//                    if (e != null || snapshots == null) return;
//
//                    activityLogList.clear();
//
//                    for (QueryDocumentSnapshot document : snapshots) {
//                        String activity = document.getString("activity");
//
//                        Date timestamp = null;
//                        Log.d("ActivityLogDebug", "Doc ID: " + document.getId());
//                        Log.d("ActivityLogDebug", "Activity: " + activity);
//                        Log.d("ActivityLogDebug", "Timestamp (Date): " + timestamp);
//                        Log.d("ActivityLogDebug", "Raw timestamp: " + document.get("timestamp"));
//
//                        Object rawTimestamp = document.get("timestamp");
//                        if (rawTimestamp instanceof com.google.firebase.Timestamp) {
//                            timestamp = ((com.google.firebase.Timestamp) rawTimestamp).toDate();
//                        } else if (rawTimestamp instanceof Date) {
//                            timestamp = (Date) rawTimestamp;
//                        } else if (rawTimestamp instanceof Long) {
//                            timestamp = new Date((Long) rawTimestamp);
//                        } else if (rawTimestamp instanceof String) {
//                            try {
//                                timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse((String) rawTimestamp);
//                            } catch (Exception parseException) {
//                                Log.e("ProfileView", "Failed to parse timestamp string", parseException);
//                            }
//                        }
//
//                        if (activity != null && timestamp != null) {
//                            activityLogList.add(new ActivityLog(uid, activity, timestamp));
//                        }
//                    }
//
//
//                    activityAdapter.notifyDataSetChanged();
//                });
//    }
//
//
//
//
//
//    private void updateTaskStatistics(int completed, int pending) {
//        int total = completed + pending;
//
//        completedTasksCount.setText(String.valueOf(completed));
//        pendingTasksCount.setText(String.valueOf(pending));
//        totalTasksCount.setText(String.valueOf(total));
//
//        if (total > 0) {
//            int progressPercentage = (completed * 100) / total;
//            taskCompletionProgress.setProgress(progressPercentage);
//            progressPercentageTextView.setText(progressPercentage + "%");
//        } else {
//            taskCompletionProgress.setProgress(0);
//            progressPercentageTextView.setText("0%");
//        }
//
//        updateTaskChart(completed, pending);
//    }
//
//    private void setupEmptyChart() {
//        taskStatusChart.setDescription(null);
//        taskStatusChart.setHoleRadius(40f);
//        taskStatusChart.setTransparentCircleRadius(45f);
//        taskStatusChart.setDrawEntryLabels(false);
//        taskStatusChart.setCenterText("TASKS");
//        taskStatusChart.setCenterTextSize(16f);
//        taskStatusChart.setCenterTextColor(Color.parseColor("#80FFFF"));
//        taskStatusChart.setHoleColor(Color.TRANSPARENT);
//        taskStatusChart.setTransparentCircleColor(Color.parseColor("#7B68EE"));
//        taskStatusChart.setNoDataText("NO MISSION DATA");
//        taskStatusChart.setNoDataTextColor(Color.parseColor("#80FFFF"));
//
//        Legend legend = taskStatusChart.getLegend();
//        legend.setTextColor(Color.parseColor("#E0FFFF"));
//        legend.setTextSize(12f);
//        legend.setForm(Legend.LegendForm.CIRCLE);
//    }
//
//    private void updateTaskChart(int completed, int pending) {
//        List<PieEntry> entries = new ArrayList<>();
//
//        if (completed > 0) {
//            entries.add(new PieEntry(completed, "COMPLETED"));
//        }
//
//        if (pending > 0) {
//            entries.add(new PieEntry(pending, "PENDING"));
//        }
//
//        if (entries.isEmpty()) {
//            taskStatusChart.setData(null);
//            taskStatusChart.invalidate();
//            return;
//        }
//
//        PieDataSet dataSet = new PieDataSet(entries, "");
//        dataSet.setColors(new int[]{
//                Color.parseColor("#00FF9C"),
//                Color.parseColor("#FF427F")
//        });
//
//        dataSet.setValueTextSize(14f);
//        dataSet.setValueTextColor(Color.WHITE);
//        dataSet.setSliceSpace(3f);
//        dataSet.setSelectionShift(6f);
//
//        PieData data = new PieData(dataSet);
//        taskStatusChart.setData(data);
//        taskStatusChart.animateY(1000);
//        taskStatusChart.invalidate();
//    }
//
//    private void setupButtonListeners() {
//        btnEditProfile.setOnClickListener(v -> {
//            Toast.makeText(this, "Edit Profile feature coming soon", Toast.LENGTH_SHORT).show();
//        });
//
//        btnViewAllTasks.setOnClickListener(v -> {
//            Toast.makeText(this, "Redirecting to tasks view", Toast.LENGTH_SHORT).show();
//        });
//    }
//
//    private void logActivityToFirestore(String activityDescription) {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null) return;
//
//        String userId = user.getUid();
//
//        Map<String, Object> log = new HashMap<>();
//        log.put("userId", userId);
//        log.put("activity", activityDescription);
//        log.put("timestamp", new Date());
//
//        FirebaseFirestore.getInstance()
//                .collection("users")
//                .document(userId)
//                .collection("activities")
//                .add(log)
//                .addOnSuccessListener(documentReference ->
//                        Log.d("ActivityLog", "Activity logged"))
//                .addOnFailureListener(e ->
//                        Log.e("ActivityLog", "Failed to log activity", e));
//    }
//
//}
package com.example.schedule_application.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_application.R;
import com.example.schedule_application.Adapter.RecentActivityAdapter;
import com.example.schedule_application.model.ActivityLog;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class ProfileView extends AppCompatActivity {
    private TextView usernameTextView, emailTextView, progressPercentageTextView;
    private TextView completedTasksCount, pendingTasksCount, totalTasksCount;
    private ProgressBar taskCompletionProgress;
    private PieChart taskStatusChart;
    private Button btnEditProfile, btnViewAllTasks;
    private RecyclerView recentActivityRecyclerView;
    private RecentActivityAdapter activityAdapter;
    private ArrayList<ActivityLog> activityLogList;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initializeViews();
        setupRecyclerView();
        loadUserProfile();
        loadTaskStatistics();
        listenToRecentActivities();
        setupButtonListeners();
    }

    private void initializeViews() {
        usernameTextView = findViewById(R.id.textUsername);
        emailTextView = findViewById(R.id.textEmail);

        completedTasksCount = findViewById(R.id.completedTasksCount);
        pendingTasksCount = findViewById(R.id.pendingTasksCount);
        totalTasksCount = findViewById(R.id.totalTasksCount);
        taskCompletionProgress = findViewById(R.id.taskCompletionProgress);
        progressPercentageTextView = findViewById(R.id.progressPercentage);

        taskStatusChart = findViewById(R.id.taskStatusChart);
        setupEmptyChart();

        recentActivityRecyclerView = findViewById(R.id.recentActivityRecyclerView);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnViewAllTasks = findViewById(R.id.btnViewAllTasks);
    }

    private void setupRecyclerView() {
        activityLogList = new ArrayList<>();
        activityAdapter = new RecentActivityAdapter(activityLogList);
        recentActivityRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentActivityRecyclerView.setAdapter(activityAdapter);
    }

    private void loadUserProfile() {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");

                        usernameTextView.setText(username);
                        emailTextView.setText(email);
                    } else {
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadTaskStatistics() {
        taskCompletionProgress.setVisibility(ProgressBar.VISIBLE);

        db.collection("users")
                .document(uid)
                .collection("tasks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int completed = 0;
                        int pending = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String status = document.getString("status");
                            if ("Completed".equalsIgnoreCase(status)) {
                                completed++;
                            } else {
                                pending++;
                            }
                        }

                        updateTaskStatistics(completed, pending);
                    } else {
                        Toast.makeText(this, "Error loading tasks: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void listenToRecentActivities() {
        db.collection("users")
                .document(uid)
                .collection("activities")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    activityLogList.clear();

                    for (QueryDocumentSnapshot document : snapshots) {
                        String activity = document.getString("activity");

                        Date timestamp = null;
                        Log.d("ActivityLogDebug", "Doc ID: " + document.getId());
                        Log.d("ActivityLogDebug", "Activity: " + activity);
                        Log.d("ActivityLogDebug", "Timestamp (Date): " + timestamp);
                        Log.d("ActivityLogDebug", "Raw timestamp: " + document.get("timestamp"));

                        Object rawTimestamp = document.get("timestamp");
                        if (rawTimestamp instanceof com.google.firebase.Timestamp) {
                            timestamp = ((com.google.firebase.Timestamp) rawTimestamp).toDate();
                        } else if (rawTimestamp instanceof Date) {
                            timestamp = (Date) rawTimestamp;
                        } else if (rawTimestamp instanceof Long) {
                            timestamp = new Date((Long) rawTimestamp);
                        } else if (rawTimestamp instanceof String) {
                            try {
                                timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse((String) rawTimestamp);
                            } catch (Exception parseException) {
                                Log.e("ProfileView", "Failed to parse timestamp string", parseException);
                            }
                        }

                        if (activity != null && timestamp != null) {
                            activityLogList.add(new ActivityLog(uid, activity, timestamp));
                        }
                    }

                    activityAdapter.notifyDataSetChanged();
                });
    }

    private void updateTaskStatistics(int completed, int pending) {
        int total = completed + pending;

        completedTasksCount.setText(String.valueOf(completed));
        pendingTasksCount.setText(String.valueOf(pending));
        totalTasksCount.setText(String.valueOf(total));

        if (total > 0) {
            int progressPercentage = (completed * 100) / total;
            taskCompletionProgress.setProgress(progressPercentage);
            progressPercentageTextView.setText(progressPercentage + "%");
        } else {
            taskCompletionProgress.setProgress(0);
            progressPercentageTextView.setText("0%");
        }

        updateTaskChart(completed, pending);
    }

    private void setupEmptyChart() {
        taskStatusChart.setDescription(null);
        taskStatusChart.setHoleRadius(40f);
        taskStatusChart.setTransparentCircleRadius(45f);
        taskStatusChart.setDrawEntryLabels(false);
        taskStatusChart.setCenterText("TASKS");
        taskStatusChart.setCenterTextSize(16f);
        taskStatusChart.setCenterTextColor(Color.parseColor("#80FFFF"));
        taskStatusChart.setHoleColor(Color.TRANSPARENT);
        taskStatusChart.setTransparentCircleColor(Color.parseColor("#7B68EE"));
        taskStatusChart.setNoDataText("NO MISSION DATA");
        taskStatusChart.setNoDataTextColor(Color.parseColor("#80FFFF"));

        Legend legend = taskStatusChart.getLegend();
        legend.setTextColor(Color.parseColor("#E0FFFF"));
        legend.setTextSize(12f);
        legend.setForm(Legend.LegendForm.CIRCLE);
    }

    private void updateTaskChart(int completed, int pending) {
        List<PieEntry> entries = new ArrayList<>();

        if (completed > 0) {
            entries.add(new PieEntry(completed, "COMPLETED"));
        }

        if (pending > 0) {
            entries.add(new PieEntry(pending, "PENDING"));
        }

        if (entries.isEmpty()) {
            taskStatusChart.setData(null);
            taskStatusChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.parseColor("#00FF9C"),
                Color.parseColor("#FF427F")
        });

        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(6f);

        PieData data = new PieData(dataSet);
        taskStatusChart.setData(data);
        taskStatusChart.animateY(1000);
        taskStatusChart.invalidate();
    }

    private void setupButtonListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Edit Profile feature coming soon", Toast.LENGTH_SHORT).show();
        });

        btnViewAllTasks.setOnClickListener(v -> {
            Toast.makeText(this, "Redirecting to tasks view", Toast.LENGTH_SHORT).show();
        });
    }

    // ADD THESE NEW METHODS FOR TASK DELETION WITH LOGGING

    /**
     * Deletes a task from ProfileView and logs the activity
     */
    private void deleteTaskFromProfile(String taskId, String taskTitle) {
        Log.d("ProfileView", "Attempting to delete task: " + taskTitle + " (ID: " + taskId + ")");

        db.collection("users")
                .document(uid)
                .collection("tasks")
                .document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Log the activity AFTER successful deletion
                    logActivityToFirestore("Deleted task: " + taskTitle);

                    // Reload statistics to update the UI
                    loadTaskStatistics();

                    Log.d("ProfileView", "Task deleted successfully: " + taskTitle);
                    Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileView", "Failed to delete task: " + taskTitle, e);
                    Toast.makeText(this, "Failed to delete task", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Handles task deletion with confirmation dialog
     */
    private void handleTaskDeletion(String taskId, String taskTitle) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete '" + taskTitle + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteTaskFromProfile(taskId, taskTitle);
                })
                .setNegativeButton("Cancel", null)
                .show();
        }
    /**
     * Marks a task as completed and logs the activity
     */
    private void markTaskAsCompleted(String taskId, String taskTitle) {
        Log.d("ProfileView", "Marking task as completed: " + taskTitle);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Completed");
        updates.put("completedAt", new Date());

        db.collection("users")
                .document(uid)
                .collection("tasks")
                .document(taskId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Log the activity AFTER successful update
                    logActivityToFirestore("Completed task: " + taskTitle);

                    // Reload statistics to update the UI
                    loadTaskStatistics();

                    Log.d("ProfileView", "Task marked as completed: " + taskTitle);
                    Toast.makeText(this, "Task completed!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileView", "Failed to mark task as completed: " + taskTitle, e);
                    Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Creates a new task and logs the activity
     */
    private void createNewTask(String title, String description, String status) {
        Log.d("ProfileView", "Creating new task: " + title);

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("title", title);
        taskData.put("description", description);
        taskData.put("status", status != null ? status : "Pending");
        taskData.put("createdAt", new Date());
        taskData.put("userId", uid);

        db.collection("users")
                .document(uid)
                .collection("tasks")
                .add(taskData)
                .addOnSuccessListener(documentReference -> {
                    String taskId = documentReference.getId();

                    // Log the activity AFTER successful creation
                    logActivityToFirestore("Created task: " + title);

                    // Reload statistics to update the UI
                    loadTaskStatistics();

                    Log.d("ProfileView", "New task created with ID: " + taskId);
                    Toast.makeText(this, "Task created successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileView", "Failed to create task: " + title, e);
                    Toast.makeText(this, "Failed to create task", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Enhanced logging method with better debugging
     */
    private void logActivityToFirestore(String activityDescription) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("ProfileView", "No authenticated user for activity logging");
            return;
        }

        String userId = user.getUid();
        Log.d("ProfileView", "Logging activity: " + activityDescription);

        Map<String, Object> log = new HashMap<>();
        log.put("userId", userId);
        log.put("activity", activityDescription);
        log.put("timestamp", new Date());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("activities")
                .add(log)
                .addOnSuccessListener(documentReference -> {
                    Log.d("ProfileView", "Activity logged successfully: " + activityDescription);
                    Log.d("ProfileView", "Activity document ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileView", "Failed to log activity: " + activityDescription, e);
                });
    }

    public void deleteTask(String taskId, String taskTitle) {
        deleteTaskFromProfile(taskId, taskTitle);
    }


    public void completeTask(String taskId, String taskTitle) {
        markTaskAsCompleted(taskId, taskTitle);
    }
}

// PUBLIC METHODS FOR EXTERNAL ACCESS (if needed by adapters or other components)



