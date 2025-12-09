package com.amisadman.aybaylite.patterns.facade;

import android.content.Context;

import com.amisadman.aybaylite.Repo.DatabaseRepository;
import com.amisadman.aybaylite.model.Transaction;
import com.amisadman.aybaylite.patterns.command.Command;
import com.amisadman.aybaylite.patterns.command.DeleteCommand;
import com.amisadman.aybaylite.patterns.observer.TransactionObserver;
import com.amisadman.aybaylite.patterns.strategy.DataOperationStrategy;
import com.amisadman.aybaylite.patterns.strategy.ExpenseOperationStrategy;
import com.amisadman.aybaylite.patterns.strategy.IncomeOperationStrategy;

import java.util.List;
import java.util.Stack;


public class FinanceManager {
    private static FinanceManager instance;
    private DatabaseRepository repository;
    private Context context;
    private Stack<Command> commandHistory = new Stack<>();

    private FinanceManager(Context context) {
        this.context = context.getApplicationContext();
        // Default strategy or none
        this.repository = new DatabaseRepository(null);
    }

    public static synchronized FinanceManager getInstance(Context context) {
        if (instance == null) {
            instance = new FinanceManager(context);
        }
        return instance;
    }

    // Switch Strategy (Strategy Pattern)
    public void setStrategyType(String type) {
        if ("EXPENSE".equalsIgnoreCase(type)) {
            // Strategy Pattern: Context (Repository) initialized with Concrete Strategy (Expense)
            this.repository = new DatabaseRepository(new ExpenseOperationStrategy());
        } else if ("INCOME".equalsIgnoreCase(type)) {
            // Strategy Pattern: Context (Repository) initialized with Concrete Strategy (Income)
            this.repository = new DatabaseRepository(new IncomeOperationStrategy());
        }
    }

    public void addObserver(TransactionObserver observer) {
        if (repository != null) {
            repository.addObserver(observer);
        }
    }

    public List<Transaction> getTransactions() {
        return repository.loadTransactions(context);
    }

    public void deleteTransaction(Transaction transaction) {
        Command cmd = new DeleteCommand(repository, transaction, context);
        cmd.execute();
        commandHistory.push(cmd);
    }

    public void undoLastAction() {
        if (!commandHistory.isEmpty()) {
            Command cmd = commandHistory.pop();
            cmd.undo();
        }
    }

    public void addTransaction(Transaction transaction) {
        if (repository != null) {
            repository.addData(context, transaction);
        }
    }

    public void updateTransaction(Transaction transaction) {
        if (repository != null) {
            repository.updateData(context, transaction);
        }
    }
}