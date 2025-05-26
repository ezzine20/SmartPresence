package com.example.emsi_smartpresence.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.emsi_smartpresence.R;
import com.example.emsi_smartpresence.models.Task;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.titleText.setText(task.getTitle());
        holder.descriptionText.setText(task.getDescription());
        holder.statusText.setText(task.getStatus());
        
        if (task.getDueDate() != null) {
            holder.dueDateText.setText("Date limite: " + dateFormat.format(task.getDueDate()));
        }
        
        String priority = task.getPriority();
        if (priority != null) {
            holder.priorityText.setText(priority);
            switch (priority.toLowerCase()) {
                case "high":
                    holder.priorityText.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
                    break;
                case "medium":
                    holder.priorityText.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
                    break;
                case "low":
                    holder.priorityText.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
                    break;
            }
        }

        if (task.getWeatherCondition() != null) {
            holder.weatherText.setText("Météo: " + task.getWeatherCondition());
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView descriptionText;
        TextView statusText;
        TextView dueDateText;
        TextView priorityText;
        TextView weatherText;

        TaskViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.taskTitle);
            descriptionText = itemView.findViewById(R.id.taskDescription);
            statusText = itemView.findViewById(R.id.taskStatus);
            dueDateText = itemView.findViewById(R.id.taskDueDate);
            priorityText = itemView.findViewById(R.id.taskPriority);
            weatherText = itemView.findViewById(R.id.taskWeather);
        }
    }
} 