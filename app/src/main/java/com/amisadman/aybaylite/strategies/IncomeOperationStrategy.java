package com.amisadman.aybaylite.strategies;

import com.amisadman.aybaylite.Repo.DatabaseRepository;

import java.util.ArrayList;
import java.util.HashMap;

public class IncomeOperationStrategy implements DataOperationStrategy
{
    private final DatabaseRepository repository;

    public IncomeOperationStrategy(DatabaseRepository repository)
    {
        this.repository = repository;
    }

    @Override
    public ArrayList<HashMap<String, String>> loadData()
    {
        return repository.loadAllIncomes();
    }

    @Override
    public boolean deleteData(String id)
    {
        return repository.deleteIncome(id);
    }

    @Override
    public String getType()
    {
        return "INCOME";
    }
}
