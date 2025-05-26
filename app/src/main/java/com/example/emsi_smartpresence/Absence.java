package com.example.emsi_smartpresence;

import java.util.List;

public class Absence {
    private String date;
    private List<String> seances;
    private String motif;
    private String status;

    public Absence(String date, List<String> seances, String motif, String status) {
        this.date = date;
        this.seances = seances;
        this.motif = motif;
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public List<String> getSeances() {
        return seances;
    }

    public String getMotif() {
        return motif;
    }

    public String getStatus() {
        return status;
    }
} 