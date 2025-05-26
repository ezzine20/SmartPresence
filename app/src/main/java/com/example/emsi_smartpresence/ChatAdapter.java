package com.example.emsi_smartpresence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.graphics.Color;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.messageText.setText(message.getMessage());
        
        // Afficher le nom de l'expéditeur (uniquement pour les messages qui ne sont pas de l'utilisateur actuel)
        if (!message.isUser()) {
            holder.senderNameText.setVisibility(View.VISIBLE);
            holder.senderNameText.setText(message.getSenderName());
        } else {
            holder.senderNameText.setVisibility(View.GONE);
        }

        // Aligner le messageContainer et changer le fond en fonction de l'expéditeur
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageContainer.getLayoutParams();
        if (message.isUser()) {
            params.gravity = Gravity.END;
            holder.messageContainer.setBackgroundResource(R.drawable.bg_message_user);
            holder.messageText.setTextColor(Color.WHITE);
            holder.senderNameText.setTextColor(Color.parseColor("#B2FFFFFF"));
        } else {
            params.gravity = Gravity.START;
            holder.messageContainer.setBackgroundResource(R.drawable.bg_message_assistant);
            holder.messageText.setTextColor(Color.BLACK);
            holder.senderNameText.setTextColor(Color.parseColor("#3F51B5"));
        }
        holder.messageContainer.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView senderNameText;
        LinearLayout messageContainer;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            senderNameText = itemView.findViewById(R.id.senderNameText);
            messageContainer = itemView.findViewById(R.id.messageContainer);
        }
    }
} 