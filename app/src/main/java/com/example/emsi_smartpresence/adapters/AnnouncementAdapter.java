package com.example.emsi_smartpresence.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.emsi_smartpresence.R;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {
    private List<DocumentSnapshot> announcements = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public AnnouncementAdapter(List<DocumentSnapshot> announcements) {
        this.announcements = announcements;
    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_announcement, parent, false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
        DocumentSnapshot announcement = announcements.get(position);
        holder.titleText.setText(announcement.getString("title"));
        holder.contentText.setText(announcement.getString("content"));
        
        if (announcement.getTimestamp("timestamp") != null) {
            holder.dateText.setText(dateFormat.format(announcement.getTimestamp("timestamp").toDate()));
        }
        
        String priority = announcement.getString("priority");
        if (priority != null) {
            holder.priorityText.setText(priority);
            // Changer la couleur en fonction de la priorité
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
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    public void setAnnouncements(List<DocumentSnapshot> announcements) {
        this.announcements = announcements;
        notifyDataSetChanged();
    }

    static class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView contentText;
        TextView dateText;
        TextView priorityText;

        AnnouncementViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.announcementTitle);
            contentText = itemView.findViewById(R.id.announcementContent);
            dateText = itemView.findViewById(R.id.announcementDate);
            priorityText = itemView.findViewById(R.id.announcementPriority);
        }
    }
} 