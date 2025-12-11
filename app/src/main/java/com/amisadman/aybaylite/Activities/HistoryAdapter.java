package com.amisadman.aybaylite.Activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amisadman.aybaylite.R;
import com.amisadman.aybaylite.Repo.DatabaseHelper;
import com.amisadman.aybaylite.model.ChatSession;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<ChatSession> sessionList;
    private OnSessionClickListener listener;
    private DatabaseHelper dbHelper;

    public interface OnSessionClickListener {
        void onSessionClick(ChatSession session);
    }

    public HistoryAdapter(Context context, List<ChatSession> sessionList, OnSessionClickListener listener) {
        this.context = context;
        this.sessionList = sessionList;
        this.listener = listener;
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ChatSession session = sessionList.get(position);
        holder.tvTitle.setText(session.getTitle());

        holder.itemView.setOnClickListener(v -> listener.onSessionClick(session));

        holder.btnDelete.setOnClickListener(v -> {
            dbHelper.deleteSession(session.getSessionId());
            sessionList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, sessionList.size());
        });
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageButton btnDelete;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHistoryTitle);
            btnDelete = itemView.findViewById(R.id.btnDeleteSession);
        }
    }
}
