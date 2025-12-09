package com.amisadman.aybaylite.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.amisadman.aybaylite.R;
import com.amisadman.aybaylite.Repo.DatabaseRepository;
import com.amisadman.aybaylite.patterns.strategy.DataOperationStrategy;
import com.amisadman.aybaylite.patterns.strategy.ExpenseOperationStrategy;

import java.util.ArrayList;
import java.util.HashMap;

public class ShowExpense extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView tvTitle;
    TextView tvNoDataMessage, tvBalance, tvTotal_show;
    Button btnAddOther;
    ImageButton btnBack;
    LottieAnimationView noDataAnimation;

    // Pattern implementations
    private DatabaseRepository repository; // Singleton
    private DataOperationStrategy operationStrategy; // Strategy

    ArrayList<HashMap<String, String>> arrayList;
    HashMap<String, String> hashMap;
    MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);

        initializeViews();
        setupPatterns(); // Initialize Singleton and Strategy
        setupRecyclerView();
        setupClickListeners();

        refreshData();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvTitle = findViewById(R.id.tvTitle);
        noDataAnimation = findViewById(R.id.noDataAnimation);
        tvNoDataMessage = findViewById(R.id.tvNoDataMessage);
        btnBack = findViewById(R.id.btnBack);
        btnAddOther = findViewById(R.id.btnAddOther);
        tvBalance = findViewById(R.id.tvBalance);
        tvTotal_show = findViewById(R.id.tvTotal_show);

        tvTitle.setText("Expense Statement");
    }

    private void setupPatterns() {
        // Singleton Pattern
        repository = DatabaseRepository.getInstance(this);

        // Strategy Pattern
        operationStrategy = new ExpenseOperationStrategy(repository);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnAddOther.setOnClickListener(v -> {
            Intent intent = new Intent(ShowExpense.this, AddExpense.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        // Using Strategy pattern to load data
        arrayList = operationStrategy.loadData();
        adapter.notifyDataSetChanged();

        updateUI();

        // Update totals
        updateTotalAmount();
    }

    private void updateUI() {
        if (arrayList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noDataAnimation.setVisibility(View.VISIBLE);
            tvNoDataMessage.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noDataAnimation.setVisibility(View.GONE);
            tvNoDataMessage.setVisibility(View.GONE);
        }
    }

    private void updateTotalAmount() {
        double total = 0;
        for (HashMap<String, String> item : arrayList) {
            try {
                total += Double.parseDouble(item.get("amount"));
            } catch (NumberFormatException e) {
                // Handle parsing error
            }
        }
        tvTotal_show.setText(String.format("Total: ৳%.2f", total));
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_expense, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            hashMap = arrayList.get(position);

            String id = hashMap.get("id");
            String reason = hashMap.get("reason");
            String formattedTime = hashMap.get("time");

            double amountValue = 0;
            try {
                amountValue = Double.parseDouble(hashMap.get("amount"));
            } catch (NumberFormatException e) {
                amountValue = 0;
            }

            String formattedAmount = String.format("%.2f", amountValue);

            holder.tvReason.setText(reason);
            holder.tvAmount.setText("৳ " + formattedAmount);
            holder.tvTime.setText(formattedTime);

            holder.btnDeleteItem.setOnClickListener(v -> {
                // Using Strategy pattern to delete data
                boolean isDeleted = operationStrategy.deleteData(id);
                if (isDeleted) {
                    arrayList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, arrayList.size());

                    updateUI();
                    updateTotalAmount();

                    Toast.makeText(v.getContext(), "Expense deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(v.getContext(), "Failed to delete expense", Toast.LENGTH_SHORT).show();
                }
            });

            double finalAmountValue = amountValue;
            holder.btnEditItem.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), AddExpense.class);
                intent.putExtra("EDIT_ID", id);
                intent.putExtra("EDIT_AMOUNT", String.valueOf(finalAmountValue));
                intent.putExtra("EDIT_REASON", reason);
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvReason, tvAmount, tvTime;
            Button btnDeleteItem, btnEditItem;

            public MyViewHolder(View itemView) {
                super(itemView);
                tvReason = itemView.findViewById(R.id.tvReason);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvTime = itemView.findViewById(R.id.tvTime);
                btnDeleteItem = itemView.findViewById(R.id.btnDeleteItem);
                btnEditItem = itemView.findViewById(R.id.btnEditItem);
            }
        }
    }
}