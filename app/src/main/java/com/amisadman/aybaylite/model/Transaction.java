package com.amisadman.aybaylite.model;

import com.amisadman.aybaylite.patterns.composite.TransactionComponent;

public abstract class Transaction implements TransactionComponent
{
    protected String id;
    protected double amount;
    protected String reason;
    protected long time;

    public Transaction(String id, double amount, String reason, long time)
    {
        this.id = id;
        this.amount = amount;
        this.reason = reason;
        this.time = time;
    }

    public String getId()
    {
        return id;
    }

    public String getReason()
    {
        return reason;
    }

    public long getTime()
    {
        return time;
    }

    public void setAmount(double amount)
    {
        this.amount = amount;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public String getDetails() {
        return reason + ": " + amount;
    }

}