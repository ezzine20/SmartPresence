package com.example.emsi_smartpresence.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emsi_smartpresence.R;
import com.example.emsi_smartpresence.HistoriqueActivity.HistoryItem; // Import the inner class

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryItem> historyList;
    private SimpleDateFormat dateFormat;

    public HistoryAdapter(List<HistoryItem> historyList) {
        this.historyList = historyList;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historique, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);
        holder.itemTitle.setText(item.getTitle());
        holder.itemType.setText(item.getType());

        // Set icon based on type
        if (item.getType().equals("Réclamation")) {
            holder.itemIcon.setImageResource(R.drawable.ic_claim);
        } else if (item.getType().equals("Événement")) {
            holder.itemIcon.setImageResource(R.drawable.ic_event);
        } else {
            holder.itemIcon.setImageResource(R.drawable.ic_history);
        }

        // Convert timestamp (long) to Date string
        if (item.getTimestamp() > 0) {
            String formattedDate = dateFormat.format(new Date(item.getTimestamp()));
            holder.itemTimestamp.setText(formattedDate);
        } else {
            holder.itemTimestamp.setText(""); // Handle cases with no timestamp
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView itemTitle;
        TextView itemType;
        TextView itemTimestamp;
        ImageView itemIcon;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.itemTitle);
            itemType = itemView.findViewById(R.id.itemType);
            itemTimestamp = itemView.findViewById(R.id.itemTimestamp);
            itemIcon = itemView.findViewById(R.id.itemIcon);
        }
    }
} 