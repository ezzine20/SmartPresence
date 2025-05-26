package com.example.emsi_smartpresence.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.emsi_smartpresence.R;
import com.example.emsi_smartpresence.models.Event;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventTitleTextView.setText(event.getTitle());

        if (event.isAllDay()) {
            holder.eventTimeTextView.setVisibility(View.GONE);
        } else {
            holder.eventTimeTextView.setVisibility(View.VISIBLE);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            holder.eventTimeTextView.setText(timeFormat.format(event.getStartTime()));
        }

        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            holder.eventLocationTextView.setVisibility(View.VISIBLE);
            holder.eventLocationTextView.setText(event.getLocation());
        } else {
            holder.eventLocationTextView.setVisibility(View.GONE);
        }

        // TODO: Add click listener for item if needed to view/edit event details
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitleTextView;
        TextView eventTimeTextView;
        TextView eventLocationTextView;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitleTextView = itemView.findViewById(R.id.eventTitleTextView);
            eventTimeTextView = itemView.findViewById(R.id.eventTimeTextView);
            eventLocationTextView = itemView.findViewById(R.id.eventLocationTextView);
        }
    }
} 