package com.amisadman.aybaylite.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.amisadman.aybaylite.Controllers.DashboardManager;
import com.amisadman.aybaylite.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    TextView tvFinalBalance, tvTotalExpense, tvTotalIncome, tvUsername;
    LinearLayout btnAddExpense, btnAddIncome, btnShowAllDataIncome, btnShowAllDataExpense;
    static RecyclerView recyclerView_main;
    static TextView tvNoDataMessage_main;
    static LottieAnimationView noDataAnimation_main;
    DashboardManager manager;
    static ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    static MyAdapter adapter;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        tvFinalBalance = findViewById(R.id.tvFinalBalance);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvUsername = findViewById(R.id.tvUsername);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnAddIncome = findViewById(R.id.btnAddIncome);
        btnShowAllDataExpense = findViewById(R.id.btnShowAllDataExpense);
        btnShowAllDataIncome = findViewById(R.id.btnShowAllDataIncome);
        recyclerView_main = findViewById(R.id.recyclerView_main);
        noDataAnimation_main = findViewById(R.id.noDataAnimation_main);
        tvNoDataMessage_main = findViewById(R.id.tvNoDataMessage_main);
        ImageButton btnLogout = findViewById(R.id.logout);

        manager = new DashboardManager(this);

        // Set username
        String username = getIntent().getStringExtra("USERNAME");
        tvUsername.setText(username + "!");


        // Set click listeners
        btnAddExpense.setOnClickListener(v -> startActivity(new Intent(this, AddExpense.class)));
        btnAddIncome.setOnClickListener(v -> startActivity(new Intent(this, AddIncome.class)));
        btnShowAllDataExpense.setOnClickListener(v -> startActivity(new Intent(this, ShowExpense.class)));
        btnShowAllDataIncome.setOnClickListener(v -> startActivity(new Intent(this, ShowIncome.class)));
        btnLogout.setOnClickListener(v -> finish());

        FloatingActionButton btnWalleoChat = findViewById(R.id.btnWalleoChat);
        btnWalleoChat.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WalleoActivity.class)));
        LinearLayout btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
        });

        LinearLayout btnManageLocal = findViewById(R.id.btnManageLocal);
        btnManageLocal.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ManageLocalActivity.class));
        });

        // Setup RecyclerView
        setupRecyclerView();
        updateUI();
    }

    private void setupRecyclerView() {
        arrayList = manager.loadDataFromDatabase();
        adapter = new MyAdapter();
        recyclerView_main.setLayoutManager(new LinearLayoutManager(this));
        recyclerView_main.setAdapter(adapter);

        if (!arrayList.isEmpty()) {
            recyclerView_main.setVisibility(View.VISIBLE);
            noDataAnimation_main.setVisibility(View.GONE);
            tvNoDataMessage_main.setVisibility(View.GONE);
        } else {
            recyclerView_main.setVisibility(View.GONE);
            noDataAnimation_main.setVisibility(View.VISIBLE);
            tvNoDataMessage_main.setVisibility(View.VISIBLE);
        }
    }

    public void updateUI() {
        tvTotalIncome.setText("৳ " + manager.getTotalIncome());
        tvTotalExpense.setText("৳ " + manager.getTotalExpense());
        double balance = Math.max(0, manager.getTotalIncome() - manager.getTotalExpense());
        tvFinalBalance.setText("৳ " + balance);
    }

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        public MyAdapter() {
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvReason_main, tvAmount_main, tvTime_main;

            public MyViewHolder(View itemView) {
                super(itemView);
                tvReason_main = itemView.findViewById(R.id.tvReason_main);
                tvAmount_main = itemView.findViewById(R.id.tvAmount_main);
                tvTime_main = itemView.findViewById(R.id.tvTime_main);

            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_statement, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            HashMap<String, String> hashMap = arrayList.get(position);
            holder.tvReason_main.setText(hashMap.get("reason"));
            holder.tvTime_main.setText(hashMap.get("time"));

            String type = hashMap.get("type");
            String amount = hashMap.get("amount");
            if ("Income".equals(type)) {
                holder.tvAmount_main.setText("৳ +" + amount);
                holder.tvAmount_main.setTextColor(Color.parseColor("#228B22")); // Green
            } else {
                holder.tvAmount_main.setText("৳ " + amount);
                holder.tvAmount_main.setTextColor(Color.RED);
            }
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        arrayList = manager.loadDataFromDatabase();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateUI();

        // Update visibility based on data
        if (!arrayList.isEmpty()) {
            recyclerView_main.setVisibility(View.VISIBLE);
            noDataAnimation_main.setVisibility(View.GONE);
            tvNoDataMessage_main.setVisibility(View.GONE);
        } else {
            recyclerView_main.setVisibility(View.GONE);
            noDataAnimation_main.setVisibility(View.VISIBLE);
            tvNoDataMessage_main.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}