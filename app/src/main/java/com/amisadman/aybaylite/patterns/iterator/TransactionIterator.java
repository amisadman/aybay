package com.amisadman.aybaylite.patterns.iterator;

import com.amisadman.aybaylite.model.Transaction;

import java.util.Iterator;
import java.util.List;

public class TransactionIterator implements Iterator<Transaction> {
    private List<Transaction> transactions;
    private int position = 0;

    public TransactionIterator(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public boolean hasNext() {
        return position < transactions.size();
    }

    @Override
    public Transaction next() {
        if (hasNext()) {
            return transactions.get(position++);
        }
        return null;
    }
}