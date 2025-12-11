package com.amisadman.aybaylite.Activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amisadman.aybaylite.R;
import com.amisadman.aybaylite.model.Message;
import com.google.android.material.card.MaterialCardView;

import io.noties.markwon.Markwon;
import android.content.Context;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    List<Message> messageList;
    Context context;
    Markwon markwon;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
        this.markwon = Markwon.create(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chatView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, null);
        chatView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new MyViewHolder(chatView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message.getSentBy().equals(Message.SEND_BY_ME)) {
            holder.left_chat_view.setVisibility(View.GONE);
            holder.right_chat_view.setVisibility(View.VISIBLE);
            holder.right_chat_text_view.setText(message.getMessage());
        } else {
            holder.right_chat_view.setVisibility(View.GONE);
            holder.left_chat_view.setVisibility(View.VISIBLE);
            // Use Markwon for bot responses
            markwon.setMarkdown(holder.left_chat_text_view, message.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView left_chat_view, right_chat_view;
        TextView left_chat_text_view, right_chat_text_view;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            left_chat_view = itemView.findViewById(R.id.left_chat_view);
            right_chat_view = itemView.findViewById(R.id.right_chat_view);
            left_chat_text_view = itemView.findViewById(R.id.left_chat_text_view);
            right_chat_text_view = itemView.findViewById(R.id.right_chat_text_view);
        }
    }
}
