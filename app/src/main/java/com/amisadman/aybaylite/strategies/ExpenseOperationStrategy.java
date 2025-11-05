package com.amisadman.aybaylite.strategies;

import com.amisadman.aybaylite.Repo.DatabaseRepository;
import java.util.ArrayList;
import java.util.HashMap;

public class ExpenseOperationStrategy implements DataOperationStrategy {
    private final DatabaseRepository repository;

    public ExpenseOperationStrategy(DatabaseRepository repository) {
        this.repository = repository;
    }

    @Override
    public ArrayList<HashMap<String, String>> loadData() {
        return repository.loadAllExpenses();
    }

    @Override
    public boolean deleteData(String id) {
        return repository.deleteExpense(id);
    }

    @Override
    public String getType() {
        return "EXPENSE";
    }
}