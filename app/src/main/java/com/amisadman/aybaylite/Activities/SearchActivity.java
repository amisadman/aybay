package com.amisadman.aybaylite.Activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amisadman.aybaylite.R;
import com.amisadman.aybaylite.Repo.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchActivity extends AppCompatActivity
{
    ImageButton btnBack;
    private RecyclerView recyclerView;
    private SearchAdapter searchAdapter;
    private ArrayList<HashMap<String, String>> searchResults;
    private TextView tvNoDataMessage;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack = findViewById(R.id.btnBack);
        dbHelper = DatabaseHelper.getInstance(this);
        recyclerView = findViewById(R.id.recyclerView);
        EditText etSearch = findViewById(R.id.etSearch);
        tvNoDataMessage = findViewById(R.id.tvNoDataMessage);

        searchResults = new ArrayList<>();
        searchAdapter = new SearchAdapter(this, searchResults, dbHelper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(searchAdapter);

        btnBack.setOnClickListener(v -> onBackPressed());
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void performSearch(String query) {
        ArrayList<HashMap<String, String>> dataFromDB = dbHelper.search(query);

        searchResults.clear();
        if (dataFromDB != null && !dataFromDB.isEmpty()) {
            searchResults.addAll(dataFromDB);
            recyclerView.setVisibility(View.VISIBLE);
            tvNoDataMessage.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            tvNoDataMessage.setVisibility(View.VISIBLE);
        }
        searchAdapter.notifyDataSetChanged();
    }
}
