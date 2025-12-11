package com.amisadman.aybaylite.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.amisadman.aybaylite.R;
import com.amisadman.aybaylite.model.Transaction;
import com.amisadman.aybaylite.patterns.facade.FinanceManager;
import com.amisadman.aybaylite.patterns.factory.TransactionFactory;

public class AddIncome extends AppCompatActivity {
    TextView tvTitle;
    EditText edAmount, edReason, edDate;
    java.util.Calendar calendar = java.util.Calendar.getInstance();
    Button button;
    ImageButton btnBack;
    LottieAnimationView animationAdd, animationUpdate;
    String editId = null;
    boolean isEdit = false;
    FinanceManager financeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_data_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Facade
        financeManager = FinanceManager.getInstance(this);
        financeManager.setStrategyType("INCOME"); // Set strategy

        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        edAmount = findViewById(R.id.edAmount);
        edReason = findViewById(R.id.edReason);
        edDate = findViewById(R.id.edDate);
        button = findViewById(R.id.button);
        animationAdd = findViewById(R.id.animationAdd);
        animationUpdate = findViewById(R.id.animationUpdate);

        Intent intent = getIntent();
        if (intent.hasExtra("EDIT_ID")) {
            isEdit = true;
            editId = intent.getStringExtra("EDIT_ID");
            edAmount.setText(intent.getStringExtra("EDIT_AMOUNT"));
            edReason.setText(intent.getStringExtra("EDIT_REASON"));

            long time = intent.getLongExtra("EDIT_TIME", 0);
            if (time != 0) {
                calendar.setTimeInMillis(time);
                updateLabel();
            }

            tvTitle.setText("Edit Income");

            animationUpdate.setVisibility(View.VISIBLE);
            animationAdd.setVisibility(View.GONE);
            button.setText("Update");
        } else {
            tvTitle.setText("Add Income");
            animationUpdate.setVisibility(View.GONE);
            animationAdd.setVisibility(View.VISIBLE);
            // Default to current time for new entry
            calendar = java.util.Calendar.getInstance();
            updateLabel();
        }

        edDate.setOnClickListener(v -> {
            new android.app.DatePickerDialog(AddIncome.this, (view, year, month, dayOfMonth) -> {
                calendar.set(java.util.Calendar.YEAR, year);
                calendar.set(java.util.Calendar.MONTH, month);
                calendar.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth);

                new android.app.TimePickerDialog(AddIncome.this, (view1, hourOfDay, minute) -> {
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(java.util.Calendar.MINUTE, minute);
                    updateLabel();
                }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), false).show();
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH),
                    calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        btnBack.setOnClickListener(v -> onBackPressed());
        button.setOnClickListener(v -> {
            String sAmount = edAmount.getText().toString();
            String reason = edReason.getText().toString();

            if (sAmount.isEmpty() || reason.isEmpty()) {
                Toast.makeText(AddIncome.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            double amount = Double.parseDouble(sAmount);
            long time = calendar.getTimeInMillis();

            try {
                if (isEdit) {
                    Transaction t = TransactionFactory.createTransaction("INCOME", editId, amount, reason, time);
                    financeManager.updateTransaction(t);
                    Toast.makeText(AddIncome.this, "Entry Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Transaction t = TransactionFactory.createTransaction("INCOME", null, amount, reason, time);
                    financeManager.addTransaction(t);
                    Toast.makeText(AddIncome.this, "Income Added!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (IllegalArgumentException e) {
                Toast.makeText(AddIncome.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(AddIncome.this, "Operation failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    private void updateLabel() {
        String myFormat = "dd-MM-yyyy hh:mm:ss a";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(myFormat, java.util.Locale.getDefault());
        edDate.setText(sdf.format(calendar.getTime()));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}