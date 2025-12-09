package com.amisadman.aybaylite.patterns.strategy;

import android.content.Context;
import com.amisadman.aybaylite.model.Transaction;
import java.util.List;

public interface DataOperationStrategy
{
    List<Transaction> loadTransactions(Context context);

    boolean deleteTransaction(Context context, String id);

    void addTransaction(Context context, Transaction transaction);

    void updateTransaction(Context context, Transaction transaction);
}
