package com.example.schedule_application.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.schedule_application.R;
import com.example.schedule_application.model.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

public class CountdownTimerActivity extends AppCompatActivity {

    private TextView taskNameDisplay;
    private TextView timerTextView;
    private TextView totalDurationTextView;
    private TextView statusTextView;
    private ProgressBar timerProgressBar;
    private Button pauseResumeButton;
    private Button cancelButton;

    private CountDownTimer countDownTimer;
    private long totalTimeMillis;
    private long timeRemainingMillis;
    private boolean isPaused = false;
    private Animation blinkAnimation;
    private FirebaseFirestore firestore;
    private FirebaseUser user;


    private Task task;  // make it global so we can restart task later if needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown_timer_activity);
        firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        taskNameDisplay = findViewById(R.id.taskNameDisplay);
        timerTextView = findViewById(R.id.timerTextView);
        totalDurationTextView = findViewById(R.id.totalDurationTextView);
        statusTextView = findViewById(R.id.statusTextView);
        timerProgressBar = findViewById(R.id.timerProgressBar);
        pauseResumeButton = findViewById(R.id.pauseResumeButton);
        cancelButton = findViewById(R.id.cancelButton);

        blinkAnimation = new AlphaAnimation(0.0f, 1.0f);
        blinkAnimation.setDuration(500);
        blinkAnimation.setStartOffset(20);
        blinkAnimation.setRepeatMode(Animation.REVERSE);
        blinkAnimation.setRepeatCount(Animation.INFINITE);

        Object obj = getIntent().getSerializableExtra("tasks");
        if (obj instanceof Task) {
            task = (Task) obj;
            Log.d("IntentDebug", "Task received: " + task.getName());
            // Use task here
        } else {
            Log.e("IntentDebug", "Failed to receive Task object");
        }


        // 📝 Display task info
        taskNameDisplay.setText(task.getName());

        try {
            int durationInMinutes = Integer.parseInt(task.getDuration());
            totalTimeMillis = durationInMinutes * 60 * 1000L;
            timeRemainingMillis = totalTimeMillis;

            totalDurationTextView.setText(durationInMinutes + " minutes");

            timerProgressBar.setMax(100);
            timerProgressBar.setProgress(100);

            startTimer();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid task duration", Toast.LENGTH_SHORT).show();
            finish();
        }

        pauseResumeButton.setOnClickListener(v -> {
            if (isPaused) {
                resumeTimer();
            } else {
                pauseTimer();
            }
        });

        cancelButton.setOnClickListener(v -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            finish();
        });
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeRemainingMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemainingMillis = millisUntilFinished;
                updateTimerUI(millisUntilFinished);
                task.setStatus("Ongoing");
                updateTaskStatusInFirestore();

            }

            @Override
            public void onFinish() {
                timerTextView.setText("00:00");
                timerProgressBar.setProgress(0);

                statusTextView.setText("COMPLETED");
                statusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                statusTextView.startAnimation(blinkAnimation);

                pauseResumeButton.setText("RESTART");
                isPaused = true;
                task.setStatus("Complete");
                updateTaskStatusInFirestore();

//                logTaskStarted(task.getName());

                Toast.makeText(CountdownTimerActivity.this,
                        "MISSION OBJECTIVE COMPLETED", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void updateTimerUI(long millisUntilFinished) {
        long minutes = millisUntilFinished / 1000 / 60;
        long seconds = (millisUntilFinished / 1000) % 60;
        timerTextView.setText(String.format("%02d:%02d", minutes, seconds));

        int progress = (int) (millisUntilFinished * 100 / totalTimeMillis);
        timerProgressBar.setProgress(progress);
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isPaused = true;
        pauseResumeButton.setText("RESUME");
        statusTextView.setText("PAUSED");
//        logTaskPaused(task.getName());
        task.setStatus("Paused");
        updateTaskStatusInFirestore();


        statusTextView.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
    }

    private void resumeTimer() {
        startTimer();
        isPaused = false;
        pauseResumeButton.setText("PAUSE");
        statusTextView.setText("IN PROGRESS");
//        logTaskResumed(task.getName());
        task.setStatus("Ongoing");
        updateTaskStatusInFirestore();
        statusTextView.setTextColor(getResources().getColor(android.R.color.holo_blue_bright));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void updateTaskStatusInFirestore() {
        firestore.collection("users")
                .document(user.getUid())  // Use correct user ID
                .collection("tasks")
                .document(task.getId())  // Use task ID to target correct task
                .set(task)  // Set the updated task object
                .addOnSuccessListener(aVoid -> Log.d("TaskStatusUpdate", "Status updated to: " + task.getStatus()))
                .addOnFailureListener(e -> Log.e("TaskStatusError", "Failed to update status: " + e.getMessage()));
    }

}
