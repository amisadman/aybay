package com.amisadman.aybaylite.Controllers;

import android.content.Context;


import com.amisadman.aybaylite.Repo.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DashboardManager {
    private final DatabaseHelper dbHelper;

    // Accept DatabaseHelper directly for better testability
    public DashboardManager(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Keep original constructor for production code
    public DashboardManager(Context context) {
        this(DatabaseHelper.getInstance(context));
    }

    public ArrayList<HashMap<String, String>> loadDataFromDatabase()
    {
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        arrayList.clear(); // Clear previous data
        arrayList = dbHelper.getStatement();
        return arrayList;



    }
    public double getTotalIncome(){
        return  dbHelper.calculateTotalIncome();
    }
    public double getTotalExpense(){
        return dbHelper.calculateTotalExpense();
    }

}
