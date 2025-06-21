package com.example.schedule_application.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedule_application.R;
import com.example.schedule_application.model.ActivityLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ActivityViewHolder> {

    private ArrayList<ActivityLog> activityLogs;

    public RecentActivityAdapter(ArrayList<ActivityLog> activityLogs) {
        this.activityLogs = activityLogs;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_log, parent, false);
        return new ActivityViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityLog currentItem = activityLogs.get(position);

        holder.activityText.setText(currentItem.getActivity());

        Date timestamp = currentItem.getTimestamp();
        if (timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            String formattedDate = sdf.format(timestamp);
            holder.timestampText.setText(formattedDate);
            holder.timestampText.setVisibility(View.VISIBLE);
        } else {
            holder.timestampText.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return activityLogs.size();
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        public TextView activityText, timestampText;

        public ActivityViewHolder(View view) {
            super(view);
            activityText = view.findViewById(R.id.textActivity);
            timestampText = view.findViewById(R.id.textTimestamp);
        }
    }
}
