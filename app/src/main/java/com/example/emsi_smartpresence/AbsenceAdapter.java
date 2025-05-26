package com.example.emsi_smartpresence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import java.util.List;

public class AbsenceAdapter extends RecyclerView.Adapter<AbsenceAdapter.AbsenceViewHolder> {
    private List<Absence> absences;

    public AbsenceAdapter(List<Absence> absences) {
        this.absences = absences;
    }

    @NonNull
    @Override
    public AbsenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_absence, parent, false);
        return new AbsenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AbsenceViewHolder holder, int position) {
        Absence absence = absences.get(position);
        holder.dateText.setText(absence.getDate());
        holder.statusChip.setText(absence.getStatus());
        
        // Configuration du statut
        switch (absence.getStatus()) {
            case "Validée":
                holder.statusChip.setChipBackgroundColorResource(android.R.color.holo_green_light);
                holder.statusChip.setTextColor(holder.itemView.getContext().getColor(android.R.color.black));
                break;
            case "Refusée":
                holder.statusChip.setChipBackgroundColorResource(android.R.color.holo_red_light);
                holder.statusChip.setTextColor(holder.itemView.getContext().getColor(android.R.color.white));
                break;
            default:
                holder.statusChip.setChipBackgroundColorResource(android.R.color.holo_orange_light);
                holder.statusChip.setTextColor(holder.itemView.getContext().getColor(android.R.color.black));
                break;
        }

        // Affichage des séances
        StringBuilder seancesText = new StringBuilder();
        for (String seance : absence.getSeances()) {
            if (seancesText.length() > 0) {
                seancesText.append(", ");
            }
            seancesText.append(seance);
        }
        holder.seancesText.setText(seancesText.toString());

        // Affichage du motif si présent
        if (absence.getMotif() != null && !absence.getMotif().isEmpty()) {
            holder.motifText.setVisibility(View.VISIBLE);
            holder.motifText.setText(absence.getMotif());
        } else {
            holder.motifText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return absences.size();
    }

    static class AbsenceViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;
        Chip statusChip;
        TextView seancesText;
        TextView motifText;

        AbsenceViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            statusChip = itemView.findViewById(R.id.statusChip);
            seancesText = itemView.findViewById(R.id.seancesText);
            motifText = itemView.findViewById(R.id.motifText);
        }
    }
} 