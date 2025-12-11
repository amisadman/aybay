package com.amisadman.aybaylite.Repo;

import android.content.Context;

import com.amisadman.aybaylite.patterns.observer.TransactionObserver;
import com.amisadman.aybaylite.model.Transaction;
import com.amisadman.aybaylite.patterns.strategy.DataOperationStrategy;

import java.util.List;

public class DatabaseRepository {
    private final DataOperationStrategy dataOperationStrategy;
    private List<TransactionObserver> observers = new java.util.ArrayList<>();

    public DatabaseRepository(DataOperationStrategy dataOperationStrategy) {
        this.dataOperationStrategy = dataOperationStrategy;
    }

    // Observer Pattern: Attach
    public void addObserver(TransactionObserver observer) {
        observers.add(observer);
    }

    // Observer Pattern: Notify
    private void notifyObservers() {
        for (TransactionObserver observer : observers) {
            observer.onDataChanged();
        }
    }

    public List<Transaction> loadTransactions(Context context) {
        if (dataOperationStrategy == null)
            throw new IllegalStateException("Strategy not set");
        return dataOperationStrategy.loadTransactions(context);
    }

    public boolean deleteTransaction(Context context, String id) {
        if (dataOperationStrategy == null)
            throw new IllegalStateException("Strategy not set");
        boolean result = dataOperationStrategy.deleteTransaction(context, id);
        if (result)
            notifyObservers(); // Notify on change
        return result;
    }

    public void addData(Context context, Transaction transaction) {
        if (dataOperationStrategy == null)
            throw new IllegalStateException("Strategy not set");
        dataOperationStrategy.addTransaction(context, transaction);
        notifyObservers(); // Notify on change
    }

    public void updateData(Context context, Transaction transaction) {
        if (dataOperationStrategy == null)
            throw new IllegalStateException("Strategy not set");
        dataOperationStrategy.updateTransaction(context, transaction);
        notifyObservers(); // Notify on change
    }
}