package com.amisadman.aybaylite.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.amisadman.aybaylite.R;
import com.amisadman.aybaylite.patterns.adapter.CurrencyAdapter;
import com.amisadman.aybaylite.patterns.adapter.DateAdapter;
import com.amisadman.aybaylite.patterns.composite.CategoryComposite;
import com.amisadman.aybaylite.patterns.facade.FinanceManager;
import com.amisadman.aybaylite.patterns.iterator.TransactionIterator;
import com.amisadman.aybaylite.patterns.observer.TransactionObserver;
import com.amisadman.aybaylite.model.Transaction;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class ShowIncome extends AppCompatActivity implements TransactionObserver
{
    private RecyclerView recyclerView;
    private TextView tvTitle, tvNoDataMessage, tvBalance, tvTotal_show;
    private Button btnAddOther;
    private ImageButton btnBack;
    private LottieAnimationView noDataAnimation;

    private java.util.List<Transaction> transactionList = new ArrayList<>();
    private MyAdapter adapter;

    private FinanceManager financeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.showdata_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupPatterns();
        setupRecyclerView();
        setupClickListeners();

        refreshData();
    }

    @SuppressLint("SetTextI18n")
    private void initializeViews()
    {
        recyclerView = findViewById(R.id.recyclerView);
        tvTitle = findViewById(R.id.tvTitle);
        noDataAnimation = findViewById(R.id.noDataAnimation);
        tvNoDataMessage = findViewById(R.id.tvNoDataMessage);
        btnBack = findViewById(R.id.btnBack);
        btnAddOther = findViewById(R.id.btnAddOther);
        tvBalance = findViewById(R.id.tvBalance);
        tvTotal_show = findViewById(R.id.tvTotal_show);

        tvTitle.setText("Income Statement");
    }

    private void setupPatterns()
    {
        // Facade Pattern
        financeManager = FinanceManager.getInstance(this);
        // Strategy Pattern: Set strategy via Facade
        financeManager.setStrategyType("INCOME");
        // Observer Pattern: Register for updates
        financeManager.addObserver(this);
    }

    private void setupRecyclerView()
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners()
    {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnAddOther.setOnClickListener(v -> {
            Intent intent = new Intent(ShowIncome.this, AddIncome.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onDataChanged()
    {
        runOnUiThread(this::refreshData);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshData()
    {
        transactionList = financeManager.getTransactions();

        updateUI();
        updateTotalAmount();
        adapter.notifyDataSetChanged();
    }

    private void updateUI() {
        if (transactionList == null || transactionList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noDataAnimation.setVisibility(View.VISIBLE);
            tvNoDataMessage.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noDataAnimation.setVisibility(View.GONE);
            tvNoDataMessage.setVisibility(View.GONE);
        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void updateTotalAmount()
    {
        CategoryComposite root = new CategoryComposite("Total Income");

        // Iterator Pattern: Traverse list
        TransactionIterator iterator = new TransactionIterator(transactionList);
        while (iterator.hasNext())
        {
            root.add(iterator.next());
        }
        // Adapter Pattern: Format Currency
        CurrencyAdapter currencyAdapter = new CurrencyAdapter();
        tvTotal_show.setText("Total Income: " + currencyAdapter.format(root.getAmount()));
    }


    // =======================================================
    // Inner Adapter class
    // =======================================================
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{

        private final CurrencyAdapter currencyAdapter;
        private final DateAdapter dateAdapter;

        public MyAdapter(Context context)
        {
            this.currencyAdapter = new CurrencyAdapter();
            this.dateAdapter = new DateAdapter();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_income, parent, false);
            return new MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Transaction transaction = transactionList.get(position);

            holder.tvReason.setText(transaction.getReason());
            holder.tvAmount.setText(currencyAdapter.format(transaction.getAmount()));

            if (transaction.getTime() != 0) {
                holder.tvTime.setText(dateAdapter.format(transaction.getTime()));
            } else {
                holder.tvTime.setText("");
            }

            holder.btnDeleteItem.setOnClickListener(v -> {
                financeManager.deleteTransaction(transaction);
                Toast.makeText(v.getContext(), "Income deleted", Toast.LENGTH_SHORT).show();
                Snackbar.make(v, "Item Deleted", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                        .setAction("UNDO", view -> financeManager.undoLastAction())
                        .show();
            });

            holder.btnEditItem.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), AddIncome.class);
                intent.putExtra("EDIT_ID", transaction.getId());
                intent.putExtra("EDIT_AMOUNT", String.valueOf(transaction.getAmount()));
                intent.putExtra("EDIT_REASON", transaction.getReason());
                intent.putExtra("EDIT_TIME", transaction.getTime());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return transactionList != null ? transactionList.size() : 0;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvReason, tvAmount, tvTime;
            Button btnDeleteItem, btnEditItem;

            public MyViewHolder(@NonNull View itemView) {
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
