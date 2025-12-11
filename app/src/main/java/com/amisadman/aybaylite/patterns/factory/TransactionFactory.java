package com.amisadman.aybaylite.patterns.factory;

import com.amisadman.aybaylite.model.Expense;
import com.amisadman.aybaylite.model.Income;
import com.amisadman.aybaylite.model.Transaction;

public class TransactionFactory
{
    public static Transaction createTransaction(String type, String id, double amount, String reason, long time)
    {
        if(type.equalsIgnoreCase("INCOME"))
        {
            return new Income(id, amount, reason, time);
        }
        else if(type.equalsIgnoreCase("EXPENSE"))
        {
            return new Expense(id, amount, reason, time);
        }
        return null;

    }

}
