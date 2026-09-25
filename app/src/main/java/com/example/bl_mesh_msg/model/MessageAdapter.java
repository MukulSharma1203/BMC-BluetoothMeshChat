package com.example.bl_mesh_msg.model;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<Message> messageList = new ArrayList<>();

    public void addMessage(Message message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView textView = new TextView(parent.getContext());
        textView.setPadding(16, 12, 16, 12);
        textView.setTextSize(13f);
        textView.setTextColor(0xFF00FF66); // Neon Green
        textView.setTypeface(Typeface.MONOSPACE);
        return new MessageViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messageList.get(position);
        String formattedText = msg.getSenderId() + " : " + msg.getText();
        holder.textView.setText(formattedText);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textView = (TextView) itemView;
        }
    }
}