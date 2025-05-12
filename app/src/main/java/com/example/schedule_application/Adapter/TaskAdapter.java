package com.example.schedule_application.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.animation.ValueAnimator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_application.R;
import com.example.schedule_application.activities.CountdownTimerActivity;
import com.example.schedule_application.activities.EditActivity;
import com.example.schedule_application.activities.TaskDetailActivity;
import com.example.schedule_application.model.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context context;
    private List<Task> taskList;
    private OnTaskClickListener taskClickListener;



    // CLEAN constructor — we pass context + list + listener ALL in one
    public TaskAdapter(Context context, List<Task> taskList, OnTaskClickListener taskClickListener) {
        this.context = context;
        this.taskList = taskList;
        this.taskClickListener = taskClickListener;
    }
    public TaskAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
    }
    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskNameTextView.setText(task.getName());
        holder.taskDescriptionTextView.setText(task.getDescription());
        holder.taskCategoryTextView.setText(task.getCategory());
        holder.taskTimeTextView.setText(task.getTime());

        holder.deleteButton.setOnClickListener(v -> {
            if (taskClickListener != null) {
                taskClickListener.onTaskDelete(task.getId());
                Log.d("TaskAdapter", "Task ID: " + task.getId());
                Log.d("TaskAdapter", "Binding task: " + task.getName());



            }
        });
        holder.itemView.setOnClickListener(v -> {
            Log.d("TaskAdapter", "Binding task: " + task.getName() + " | ID: " + task.getId());

            Intent intent = new Intent(context, TaskDetailActivity.class);
            intent.putExtra("tasks", task);  // Directly send the Task object
            context.startActivity(intent);
        });
        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditActivity.class);
            intent.putExtra("taskId", task.getId());
            context.startActivity(intent);
        });
        holder.startTaskButton.setOnClickListener(v -> {
            // Create an Intent to start CountdownTimerActivity
//            Intent intent = new Intent(v.getContext(), CountdownTimerActivity.class);
//            // Pass the task object to the next Activity
//            intent.putExtra("tasks", task); // Ensure Task implements Serializable
//            v.getContext().startActivity(intent);
            Intent intent = new Intent(v.getContext(), TaskDetailActivity.class);
            intent.putExtra("tasks", task);
            v.getContext().startActivity(intent);
            Log.e("Starttt", "Activity started"+ v.getContext() );
        });

        holder.itemView.setOnClickListener(v -> {
            if (taskClickListener != null) {
                taskClickListener.onTaskUpdate(task.getId());
            }
        });


        // Animation 1 — Gradient rotation
        Animation rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.gradient_animation);
        View gradientBorder = holder.itemView.findViewById(R.id.gradientBorder);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        gradientBorder.startAnimation(rotateAnimation);

        // Animation 2 — Card touch elevation
        CardView taskCard = holder.itemView.findViewById(R.id.taskCardView);
        taskCard.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ObjectAnimator.ofFloat(taskCard, "cardElevation", 8f, 16f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    ObjectAnimator.ofFloat(taskCard, "cardElevation", 16f, 8f).setDuration(100).start();
                    break;
            }
            return false;
        });

        // Animation 3 — Category pulse
        TextView categoryChip = holder.itemView.findViewById(R.id.taskCategoryTextView);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(categoryChip, "scaleX", 1.0f, 1.05f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(categoryChip, "scaleY", 1.0f, 1.05f, 1.0f);

        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.RESTART);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatMode(ValueAnimator.RESTART);
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet pulseAnimator = new AnimatorSet();
        pulseAnimator.playTogether(scaleX, scaleY);
        pulseAnimator.setDuration(2000);
        pulseAnimator.start();

    }
    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTaskList(List<Task> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }
    public void removeTaskById(String taskId) {
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getId().equals(taskId)) {
                taskList.remove(i);
                notifyItemRemoved(i);  // This is enough
                break;
            }
        }
    }



    // ViewHolder class
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskNameTextView, taskDescriptionTextView, taskCategoryTextView, taskTimeTextView;
        FrameLayout deleteButton;
        FrameLayout editButton;
        Button startTaskButton;


        public TaskViewHolder(View itemView) {
            super(itemView);
            taskNameTextView = itemView.findViewById(R.id.taskNameTextView);
            taskDescriptionTextView = itemView.findViewById(R.id.taskDescriptionTextView);
            taskCategoryTextView = itemView.findViewById(R.id.taskCategoryTextView);
            taskTimeTextView = itemView.findViewById(R.id.taskTimeTextView);
            deleteButton = itemView.findViewById(R.id.deleteButtonContainer);
            editButton = itemView.findViewById(R.id.editButtonContainer);
            startTaskButton=itemView.findViewById(R.id.startTaskButton);
        }
    }

    // Interfaced
    public interface OnTaskClickListener {
        void onTaskUpdate(String taskId);
        void onTaskDelete(String taskId);
        void onTaskClick(Task task);  // Triggered when task is clicked

    }
}
