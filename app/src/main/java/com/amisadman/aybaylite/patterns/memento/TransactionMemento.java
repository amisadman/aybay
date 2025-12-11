package com.amisadman.aybaylite.patterns.memento;

import com.amisadman.aybaylite.model.Transaction;

public class TransactionMemento {
    private final Transaction transaction;

    public TransactionMemento(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getSavedTransaction() {
        return transaction;
    }
}