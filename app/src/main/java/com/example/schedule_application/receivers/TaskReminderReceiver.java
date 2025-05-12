package com.example.schedule_application.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.schedule_application.R;
import com.example.schedule_application.activities.TaskDetailActivity;
import com.example.schedule_application.model.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class TaskReminderReceiver extends BroadcastReceiver {
    private FirebaseUser user;
    Task task;

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskId = intent.getStringExtra("taskId");
        int notificationId = taskId.hashCode();
        intent.putExtra("tasks", task);



        fetchTaskAndNotify(context, taskId);
    }

    private void fetchTaskAndNotify(Context context, String taskId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.e("TaskReminderReceiver", "User is null. Cannot fetch task.");
            return;
        }

        // Log the passed taskId right away
        Log.d("TaskReminderReceiver", "Passed taskId for reminder: " + taskId);

        // Fetch the task document
        db.collection("users")
                .document(user.getUid())
                .collection("tasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String firebaseTaskId = doc.getString("id");
                        Log.d("TaskReminderReceiver", "Firebase task id in doc: " + firebaseTaskId);
                        Log.d("TaskReminderReceiver", "Comparing - Passed: " + taskId + " | Firebase: " + firebaseTaskId);
                    }
                });

        db.collection("users")
                .document(user.getUid())
                .collection("tasks")
                .whereEqualTo("id", taskId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        Task task = documentSnapshot.toObject(Task.class);

                        if (task != null) {
                            // Log the id in the fetched task object too
                            Log.d("TaskReminderReceiver", "Fetched task id in Task object: " + task.getId());
                            showNotification(context, task);  // Show the notification
                        }
                    } else {
                        Log.e("TaskReminderReceiver", "No task found with the given ID.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("TaskReminderReceiver", "Error fetching task: " + e.getMessage());
                });
    }


    private void showNotification(Context context, Task task) {
        String taskName = task.getName();
        String taskDescription = task.getDescription();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "task_reminders_channel";

        Intent intent = new Intent(context, TaskDetailActivity.class);
        intent.putExtra("tasks", task);

        // For Android Oreo and above, create a notification channel FIRST
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Task Reminders", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for task reminders");
            notificationManager.createNotificationChannel(channel);
        }


        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle("Reminder: " + taskName)
                .setContentText(taskDescription)
                .setSmallIcon(R.drawable.notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(createTaskDetailsIntent(context, task))
                .build();

        int notificationId = task.getId().hashCode();
        notificationManager.notify(notificationId, notification);
    }

    private PendingIntent createTaskDetailsIntent(Context context, Task task) {
        Intent intent = new Intent(context, TaskDetailActivity.class);
        intent.putExtra("tasks", task);
        // Set the flag for updating the PendingIntent
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        return PendingIntent.getActivity(context, 0, intent, flags);
    }
}

