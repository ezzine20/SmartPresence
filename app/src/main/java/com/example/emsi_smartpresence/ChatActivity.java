package com.example.emsi_smartpresence;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FieldValue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.google.firebase.firestore.DocumentSnapshot;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String groupId;
    private ListenerRegistration chatListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Récupérer l'ID du groupe depuis l'intent
        groupId = getIntent().getStringExtra("groupId");
        if (groupId == null) {
            Log.e(TAG, "Group ID is missing!");
            finish(); // Fermer l'activité si l'ID du groupe est manquant
            return;
        }

        // Configuration de la barre d'outils
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // TODO: Charger le nom du groupe et le définir ici
            getSupportActionBar().setTitle("Chargement...");
        }

        // Initialisation de Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialisation des vues
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Configuration du RecyclerView et de l'adaptateur
        chatAdapter = new ChatAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true); // Pour afficher les nouveaux messages en bas
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        // Charger les messages et écouter les nouveaux messages
        loadMessages();

        // Configuration du bouton d'envoi
        sendButton.setOnClickListener(v -> sendMessage());

        // Charger le nom du groupe
        loadGroupInfo();
    }

    private void loadMessages() {
        chatListenerRegistration = db.collection("messages")
                .whereEqualTo("groupId", groupId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    // Nouveau message ajouté
                                    String senderId = dc.getDocument().getString("senderId");
                                    String messageContent = dc.getDocument().getString("message");

                                    // Récupérer le nom de l'expéditeur à partir de la collection "users"
                                    if (senderId != null) {
                                        db.collection("users").document(senderId).get()
                                            .addOnSuccessListener(userDocumentSnapshot -> {
                                                String senderName = "Utilisateur inconnu"; // Nom par défaut
                                                if (userDocumentSnapshot.exists()) {
                                                    // Assurez-vous que votre document utilisateur a un champ 'name' ou similaire
                                                    String firstName = userDocumentSnapshot.getString("firstName");
                                                    String lastName = userDocumentSnapshot.getString("lastName");
                                                    if (firstName != null && lastName != null) {
                                                        senderName = firstName + " " + lastName;
                                                    } else if (firstName != null) {
                                                        senderName = firstName;
                                                    } else if (lastName != null) {
                                                        senderName = lastName;
                                                    }
                                                }

                                                // Déterminer si le message vient de l'utilisateur actuel
                                                boolean isUser = mAuth.getCurrentUser() != null && senderId.equals(mAuth.getCurrentUser().getUid());

                                                // Ajouter le message à la liste avec le nom de l'expéditeur
                                                messages.add(new ChatMessage(messageContent, isUser, senderName));
                                                chatAdapter.notifyItemInserted(messages.size() - 1);
                                                chatRecyclerView.scrollToPosition(messages.size() - 1);
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.w(TAG, "Error loading sender info for message", e);
                                                // Ajouter le message même si le nom de l'expéditeur ne peut pas être chargé
                                                boolean isUser = mAuth.getCurrentUser() != null && senderId.equals(mAuth.getCurrentUser().getUid());
                                                messages.add(new ChatMessage(messageContent, isUser, "Erreur nom")); // Nom d'erreur
                                                chatAdapter.notifyItemInserted(messages.size() - 1);
                                                chatRecyclerView.scrollToPosition(messages.size() - 1);
                                            });
                                    } else {
                                         // Gérer le cas où senderId est null (ce qui ne devrait pas arriver avec les règles)
                                         boolean isUser = false; // Par défaut non utilisateur
                                         messages.add(new ChatMessage(messageContent, isUser, "Expéditeur inconnu"));
                                         chatAdapter.notifyItemInserted(messages.size() - 1);
                                         chatRecyclerView.scrollToPosition(messages.size() - 1);
                                    }
                                    break;
                                // TODO: Gérer les modifications et suppressions de messages si nécessaire
                            }
                        }
                    }
                });
    }

    private void sendMessage() {
        String messageContent = messageInput.getText().toString().trim();
        if (messageContent.isEmpty()) {
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vous devez être connecté pour envoyer un message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Créer un Map pour le message à ajouter à Firestore
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("groupId", groupId);
        messageData.put("senderId", currentUser.getUid());
        messageData.put("message", messageContent);
        messageData.put("timestamp", FieldValue.serverTimestamp()); // Utiliser le timestamp du serveur
        messageData.put("type", "text");

        db.collection("messages")
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Message sent with ID: " + documentReference.getId());
                    messageInput.setText(""); // Effacer le champ de saisie après l'envoi

                    // Mettre à jour le lastMessageTime du groupe dans Firestore
                    db.collection("groups").document(groupId)
                            .update("lastMessageTime", FieldValue.serverTimestamp())
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Group lastMessageTime updated"))
                            .addOnFailureListener(e -> Log.w(TAG, "Error updating group lastMessageTime", e));

                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding message", e);
                    Toast.makeText(this, "Erreur lors de l'envoi du message", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadGroupInfo() {
        db.collection("groups").document(groupId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String groupName = documentSnapshot.getString("name");
                    if (getSupportActionBar() != null && groupName != null) {
                        getSupportActionBar().setTitle(groupName);
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error loading group info", e);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Erreur");
                }
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListenerRegistration != null) {
            chatListenerRegistration.remove(); // Arrêter d'écouter les mises à jour Firestore
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 