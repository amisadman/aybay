package com.amisadman.aybaylite.patterns.strategy;

import android.content.Context;

import com.amisadman.aybaylite.Repo.DatabaseHelper;
import com.amisadman.aybaylite.model.Transaction;
import com.amisadman.aybaylite.patterns.factory.TransactionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class ExpenseOperationStrategy implements DataOperationStrategy
{

    private DatabaseHelper dbHelper;
    private final int minimum = 1;
    private final int maximum = (int) 1e9;


    // used for ExpenseOperationStrategyTest
    public void setDatabaseHelper(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    private DatabaseHelper getDbHelper(Context context)
    {
        if (dbHelper == null)
        {
            dbHelper = DatabaseHelper.getInstance(context);
        }
        return dbHelper;
    }

    @Override
    public List<Transaction> loadTransactions(Context context)
    {
        DatabaseHelper db = getDbHelper(context);
        ArrayList<HashMap<String, String>> data = db.getAllExpenses();
        List<Transaction> transactions = new ArrayList<>();

        for (HashMap<String, String> map : data)
        {
            try
            {
                String id = map.get("id");
                double amount = Double.parseDouble(map.get("amount"));
                String reason = map.get("reason");
                long time = 0;
                if (map.containsKey("timestamp"))
                {
                    try
                    {
                        time = Long.parseLong(map.get("timestamp"));
                    }
                    catch (NumberFormatException e)
                    {
                        e.printStackTrace();
                    }
                }
                transactions.add(TransactionFactory.createTransaction("EXPENSE", id, amount, reason, time));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return transactions;
    }

    @Override
    public boolean deleteTransaction(Context context, String id)
    {
        return getDbHelper(context).deleteExpense(id);
    }

    @Override
    public void addTransaction(Context context, Transaction transaction)
    {
        double amount = transaction.getAmount();
        if (amount >= minimum && amount <= maximum)
        {
            getDbHelper(context).addExpense(transaction.getAmount(), transaction.getReason(), transaction.getTime());
        }
        else
        {
            throw new IllegalArgumentException("Amount is outside valid range");
        }
    }

    @Override
    public void updateTransaction(Context context, Transaction transaction)
    {
        double amount = transaction.getAmount();
        if (amount >= minimum && amount <= maximum)
        {
            getDbHelper(context).updateExpense(transaction.getId(), transaction.getAmount(), transaction.getReason(), transaction.getTime());
        }
        else
        {
            throw new IllegalArgumentException("Amount is outside valid range");
        }
    }
}
