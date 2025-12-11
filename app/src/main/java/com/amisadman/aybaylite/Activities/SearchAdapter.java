package com.amisadman.aybaylite.Activities;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amisadman.aybaylite.R;
import com.amisadman.aybaylite.Repo.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
    private ArrayList<HashMap<String, String>> searchResults;
    private Context context;
    private DatabaseHelper dbHelper;

    public SearchAdapter(Context context, ArrayList<HashMap<String, String>> searchResults, DatabaseHelper dbHelper) {
        this.context = context;
        this.searchResults = searchResults;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        HashMap<String, String> result = searchResults.get(position);

        String id = result.get("id");
        String amount = result.get("amount");
        String reason = result.get("reason");
        String time = result.get("time");
        String type = result.get("tableName");

        String formattedTime = time; // default value if parsing fails
        try {
            long timeMillis = Long.parseLong(time); // convert string to long
            formattedTime = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss", java.util.Locale.getDefault())
                    .format(new java.util.Date(timeMillis)); // format to readable date
        } catch (NumberFormatException e) {
            e.printStackTrace(); // log error if conversion fails
        }
        // Default color
        int textColor = Color.parseColor("#000000");

        if (type.equalsIgnoreCase("income")) {
            holder.tvAmount.setText("৳ " + amount);
            holder.tvType.setText("Income");
            textColor = Color.parseColor("#228B22");
        } else if (type.equalsIgnoreCase("expense")) {
            holder.tvAmount.setText("৳ " + amount);
            holder.tvType.setText("Expense");
            textColor = Color.parseColor("#FF0000");
        } else if (type.equalsIgnoreCase("loan")) {
            holder.tvAmount.setText("৳ " + amount);
            holder.tvType.setText("Loan");
            textColor = Color.parseColor("#D22B2B");
        } else if (type.equalsIgnoreCase("owe")) {
            holder.tvType.setText("Owe");
            holder.tvAmount.setText("৳ " + amount);
            textColor = Color.parseColor("#800080");
        } else if (type.equalsIgnoreCase("savings")) {
            holder.tvType.setText("Savings");
            holder.tvAmount.setText("৳ " + amount);
            textColor = Color.parseColor("#2196F3");
        } else if (type.equalsIgnoreCase("budget")) {
            holder.tvType.setText("Budget");
            holder.tvAmount.setText("৳ " + amount);
            textColor = Color.parseColor("#000000");
        } else {
            holder.tvAmount.setText("৳ " + amount);
        }

        holder.tvAmount.setTextColor(textColor);
        holder.tvReason.setText(reason);
        holder.tvTime.setText(formattedTime);

        holder.btnDeleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    String tableName = determineTableName(result);
                    if (tableName != null) {
                        dbHelper.deleteItem(tableName, id);
                        searchResults.remove(pos);
                        notifyItemRemoved(pos);
                        notifyItemRangeChanged(pos, searchResults.size());
                    }
                }
            }
        });
    }

    private String determineTableName(HashMap<String, String> result) {
        return result.containsKey("tableName") ? result.get("tableName") : "default_table";
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView tvReason, tvAmount, tvTime, tvType;
        Button btnDeleteItem, btnEditItem;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvType = itemView.findViewById(R.id.tvType);
            btnDeleteItem = itemView.findViewById(R.id.btnDeleteItem);
            btnEditItem = itemView.findViewById(R.id.btnEditItem);
        }
    }
}
