package com.amisadman.aybaylite.Repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseRepository {
    private static DatabaseRepository instance;
    private DatabaseHelper dbHelper;

    private DatabaseRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public static synchronized DatabaseRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseRepository(context);
        }
        return instance;
    }

    // Expense operations
    public ArrayList<HashMap<String, String>> loadAllExpenses() {
        return dbHelper.getAllExpenses();
    }

    public boolean deleteExpense(String id) {
        return dbHelper.deleteExpense(id);
    }

    // Income operations
    public ArrayList<HashMap<String, String>> loadAllIncomes() {
        return dbHelper.getAllIncome();
    }

    public boolean deleteIncome(String id) {
        return dbHelper.deleteIncome(id);
    }
}