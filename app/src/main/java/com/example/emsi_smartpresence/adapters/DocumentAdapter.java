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

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private List<DocumentSnapshot> documents = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public DocumentAdapter(List<DocumentSnapshot> documents) {
        this.documents = documents;
    }

    public void setDocuments(List<DocumentSnapshot> documents) {
        this.documents = documents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        DocumentSnapshot document = documents.get(position);
        holder.titleText.setText(document.getString("title"));
        holder.descriptionText.setText(document.getString("description"));

        if (document.getTimestamp("uploadDate") != null) {
            holder.dateText.setText(dateFormat.format(document.getTimestamp("uploadDate").toDate()));
        }

        // TODO: Implement download button click listener
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView descriptionText;
        TextView dateText;

        DocumentViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.documentTitle);
            descriptionText = itemView.findViewById(R.id.documentDescription);
            dateText = itemView.findViewById(R.id.documentDate);
        }
    }
} 