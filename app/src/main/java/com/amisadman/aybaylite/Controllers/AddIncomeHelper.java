package com.amisadman.aybaylite.Controllers;

import android.content.Context;

import com.amisadman.aybaylite.Repo.DatabaseHelper;

public class AddIncomeHelper {
    private final DatabaseHelper dbHelper;
    private  final int minimum = 1;
    private final int maximum = (int) 1e9;
    public AddIncomeHelper(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public AddIncomeHelper(Context context) {
        this(DatabaseHelper.getInstance(context));
    }
    public void updateData(String id, double amount, String reason){
        if(amount >= minimum && amount <= maximum) dbHelper.updateIncome(id, amount, reason);
        else{
            throw new IllegalArgumentException("Amount is outside valid range");
        }


    }
    public void addData(double amount,String reason){
        if(amount >= minimum && amount <= maximum) dbHelper.addIncome(amount, reason);
        else{
            throw new IllegalArgumentException("Amount is outside valid range");
        }


    }

}
