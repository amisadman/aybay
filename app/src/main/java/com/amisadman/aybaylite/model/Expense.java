package com.amisadman.aybaylite.model;

public class Expense extends Transaction
{
    public Expense(String id, double amount, String reason, long time)
    {
        super(id, amount, reason, time);
    }

}
