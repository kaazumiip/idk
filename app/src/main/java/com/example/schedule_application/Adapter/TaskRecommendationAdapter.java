package com.example.schedule_application.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_application.R;
import com.example.schedule_application.model.SuggestedTask;

import java.util.List;

public class TaskRecommendationAdapter extends RecyclerView.Adapter<TaskRecommendationAdapter.TaskViewHolder> {

    private List<SuggestedTask> taskList;
    private final OnTaskClickListener onTaskClickListener;

    // Functional interface for the click callback
    public interface OnTaskClickListener {
        void onTaskClick(SuggestedTask task);
    }

    // Constructor accepts both taskList and click listener
    public TaskRecommendationAdapter(List<SuggestedTask> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.onTaskClickListener = listener;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDescription,taskCategoryTextView,taskTime;
        Button startButton;
        FrameLayout  editButton, deleteButton;




        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskNameTextView);
            taskDescription = itemView.findViewById(R.id.taskDescriptionTextView);
            taskCategoryTextView = itemView.findViewById(R.id.taskCategoryTextView);
            taskTime = itemView.findViewById(R.id.taskTimeTextView);
            editButton = itemView.findViewById(R.id.editButtonContainer);
            deleteButton = itemView.findViewById(R.id.deleteButtonContainer);
            startButton = itemView.findViewById(R.id.startTaskButton);




        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        SuggestedTask task = taskList.get(position);
        holder.taskTitle.setText(task.getTitle());
        holder.taskDescription.setText(task.getDescription());
        holder.taskCategoryTextView.setText(task.getCategory());

        holder.taskTime.setVisibility(View.GONE);
        holder.editButton.setVisibility(View.GONE);
        holder.deleteButton.setVisibility(View.GONE);
        holder.startButton.setVisibility(View.GONE);


        holder.itemView.setOnClickListener(v -> {
            if (onTaskClickListener != null) {
                onTaskClickListener.onTaskClick(task);
            }
        });
    }


    @Override
    public int getItemCount() {
        return taskList.size();
    }
}
