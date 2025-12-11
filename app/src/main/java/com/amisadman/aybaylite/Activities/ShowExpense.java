package com.amisadman.aybaylite.Activities;

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
import java.util.List;

public class ShowExpense extends AppCompatActivity implements TransactionObserver
{

    RecyclerView recyclerView;
    TextView tvTitle;
    TextView tvNoDataMessage, tvBalance, tvTotal_show;
    Button btnAddOther;
    ImageButton btnBack;
    LottieAnimationView noDataAnimation;

    List<Transaction> transactionList = new ArrayList<>();
    MyAdapter adapter;
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

    private void setupPatterns()
    {
        // Facade Pattern: Entry point
        financeManager = FinanceManager.getInstance(this);
        // Strategy Pattern: Set strategy via Facade
        financeManager.setStrategyType("EXPENSE");
        // Observer Pattern: Register for updates
        financeManager.addObserver(this);
    }

    private void setupRecyclerView()
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(this);
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

    // Observer Pattern: Update method
    @Override
    public void onDataChanged()
    {
        runOnUiThread(this::refreshData);
    }

    private void refreshData()
    {
        // Facade: Get data
        transactionList = financeManager.getTransactions();

        updateUI();
        updateTotalAmount();
        adapter.notifyDataSetChanged();
    }

    private void updateUI()
    {
        if (transactionList == null || transactionList.isEmpty())
        {
            recyclerView.setVisibility(View.GONE);
            noDataAnimation.setVisibility(View.VISIBLE);
            tvNoDataMessage.setVisibility(View.VISIBLE);
        }
        else
        {
            recyclerView.setVisibility(View.VISIBLE);
            noDataAnimation.setVisibility(View.GONE);
            tvNoDataMessage.setVisibility(View.GONE);
        }
    }

    private void updateTotalAmount()
    {
        // Composite Pattern: Calculate Total
        CategoryComposite root = new CategoryComposite("Total Expenses");

        // Iterator Pattern: Traverse list
        TransactionIterator iterator = new TransactionIterator(transactionList);

        while (iterator.hasNext())
        {
            root.add(iterator.next());
        }

        // Adapter Pattern: Format Currency
        CurrencyAdapter currencyAdapter = new CurrencyAdapter();

        tvTotal_show.setText("Total: " + currencyAdapter.format(root.getAmount()));
    }

    // RecyclerView Adapter
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>
    {
        private final Context context;
        private CurrencyAdapter currencyAdapter;
        private DateAdapter dateAdapter;

        public MyAdapter(Context context)
        {
            this.context = context;
            this.currencyAdapter = new CurrencyAdapter();
            this.dateAdapter = new DateAdapter();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_expense, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position)
        {
            Transaction transaction = transactionList.get(position);

            String reason = transaction.getReason();

            holder.tvReason.setText(reason);
            holder.tvAmount.setText(currencyAdapter.format(transaction.getAmount()));

            if (transaction.getTime() != 0)
            {
                holder.tvTime.setText(dateAdapter.format(transaction.getTime()));
            }
            else
            {
                holder.tvTime.setText("");
            }

            holder.btnDeleteItem.setOnClickListener(v -> {
                // Command Pattern: Delete
                financeManager.deleteTransaction(transaction);
                Toast.makeText(v.getContext(), "Expense deleted", Toast.LENGTH_SHORT).show();
                // Undo
                Snackbar.make(v, "Item Deleted", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                        .setAction("UNDO", view -> financeManager.undoLastAction())
                        .show();
            });

            holder.btnEditItem.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), AddExpense.class);
                intent.putExtra("EDIT_ID", transaction.getId());
                intent.putExtra("EDIT_AMOUNT", String.valueOf(transaction.getAmount()));
                intent.putExtra("EDIT_REASON", reason);
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
