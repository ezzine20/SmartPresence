package com.example.emsi_smartpresence;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class GroupListActivity extends AppCompatActivity {
    private RecyclerView groupRecyclerView;
    private FloatingActionButton addGroupButton;
    private FirebaseFirestore db;
    private GroupAdapter groupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        // Configuration de la barre d'outils
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Groupes de discussion");
        }

        // Initialisation de Firebase
        db = FirebaseFirestore.getInstance();

        // Initialisation des vues
        groupRecyclerView = findViewById(R.id.groupRecyclerView);
        addGroupButton = findViewById(R.id.addGroupButton);

        // Configuration de l'adaptateur
        groupAdapter = new GroupAdapter(new ArrayList<>());
        groupRecyclerView.setAdapter(groupAdapter);

        // Chargement des groupes
        loadGroups();

        // Configuration du bouton d'ajout
        addGroupButton.setOnClickListener(v -> {
            // TODO: Implémenter la création d'un nouveau groupe
            startActivity(new Intent(GroupListActivity.this, NewGroupActivity.class));
        });
    }

    private void loadGroups() {
        db.collection("groups")
            .orderBy("lastMessageTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Group> groups = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Group group = new Group(
                            document.getId(),
                            document.getString("name"),
                            document.getString("description"),
                            document.getTimestamp("lastMessageTime")
                        );
                        groups.add(group);
                    }
                    groupAdapter.setGroups(groups);
                }
            });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 