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
    private SQLiteDatabase database;

    private DatabaseRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public static synchronized DatabaseRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseRepository(context);
        }
        return instance;
    }

    // Expense operations
    public ArrayList<HashMap<String, String>> loadAllExpenses() {
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM expense", null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("id", cursor.getString(0));
                hashMap.put("amount", cursor.getString(1));
                hashMap.put("reason", cursor.getString(2));
                hashMap.put("time", cursor.getString(3));
                arrayList.add(hashMap);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return arrayList;
    }

    public boolean deleteExpense(String id) {
        return database.delete("expense", "id=?", new String[]{id}) > 0;
    }

    public long insertExpense(double amount, String reason, String time) {
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("reason", reason);
        values.put("time", time);
        return database.insert("expense", null, values);
    }

    // Income operations
    public ArrayList<HashMap<String, String>> loadAllIncomes() {
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM income", null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("id", cursor.getString(0));
                hashMap.put("amount", cursor.getString(1));
                hashMap.put("reason", cursor.getString(2));
                hashMap.put("time", cursor.getString(3));
                arrayList.add(hashMap);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return arrayList;
    }

    public boolean deleteIncome(String id) {
        return database.delete("income", "id=?", new String[]{id}) > 0;
    }
}