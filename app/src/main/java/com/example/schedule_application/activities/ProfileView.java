package com.example.schedule_application.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

        loadRecentActivities();

        setupButtonListeners();
    }

    private void initializeViews() {
        // Profile information
        usernameTextView = findViewById(R.id.textUsername);
        emailTextView = findViewById(R.id.textEmail);

        // Statistics components
        completedTasksCount = findViewById(R.id.completedTasksCount);
        pendingTasksCount = findViewById(R.id.pendingTasksCount);
        totalTasksCount = findViewById(R.id.totalTasksCount);
        taskCompletionProgress = findViewById(R.id.taskCompletionProgress);
        progressPercentageTextView = findViewById(R.id.progressPercentage);

        // Chart
        taskStatusChart = findViewById(R.id.taskStatusChart);
        setupEmptyChart();

        // RecyclerView
        recentActivityRecyclerView = findViewById(R.id.recentActivityRecyclerView);

        // Buttons
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
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadTaskStatistics() {
        // Show loading state
        taskCompletionProgress.setVisibility(View.VISIBLE);

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
                            if (status != null && status.equals("Complete")) {
                                completed++;
                            } else {
                                pending++;
                            }
                        }

                        updateTaskStatistics(completed, pending);
                    } else {
                        Toast.makeText(this, "Error loading tasks: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadRecentActivities() {
        activityLogList.clear();

        db.collection("activities")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp")
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String activity = document.getString("activity");
                        Date timestamp = document.getDate("timestamp");

                        if (activity != null && timestamp != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                            String formattedDate = sdf.format(timestamp);

                            ActivityLog log = new ActivityLog(activity, formattedDate);
                            activityLogList.add(log);
                        }
                    }

                    if (activityLogList.isEmpty()) {
                        activityLogList.add(new ActivityLog("No recent activity found", ""));
                    }

                    activityAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    activityLogList.add(new ActivityLog("Error loading activities", ""));
                    activityAdapter.notifyDataSetChanged();
                });
    }

    private void updateTaskStatistics(int completed, int pending) {
        int total = completed + pending;

        // Update text views
        completedTasksCount.setText(String.valueOf(completed));
        pendingTasksCount.setText(String.valueOf(pending));
        totalTasksCount.setText(String.valueOf(total));

        // Update progress bar
        if (total > 0) {
            int progressPercentage = (completed * 100) / total;
            taskCompletionProgress.setProgress(progressPercentage);
            progressPercentageTextView.setText(progressPercentage + "%");
        } else {
            taskCompletionProgress.setProgress(0);
            progressPercentageTextView.setText("0%");
        }

        // Update chart
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
            // Intent to edit profile activity
        });

        btnViewAllTasks.setOnClickListener(v -> {
            Toast.makeText(this, "Redirecting to tasks view", Toast.LENGTH_SHORT).show();

        });
    }
    private void logActivityToFirestore(String activityDescription) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();

        Map<String, Object> log = new HashMap<>();
        log.put("userId", userId);
        log.put("activity", activityDescription);
        log.put("timestamp", new Date());

        FirebaseFirestore.getInstance()
                .collection("activities")
                .add(log)
                .addOnSuccessListener(documentReference -> Log.d("LogActivity", "Activity logged"))
                .addOnFailureListener(e -> Log.e("LogActivity", "Failed to log activity", e));
    }

}