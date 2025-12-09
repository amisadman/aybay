package com.amisadman.aybaylite.patterns.command;

import android.content.Context;
import com.amisadman.aybaylite.Repo.DatabaseRepository;
import com.amisadman.aybaylite.model.Transaction;
import com.amisadman.aybaylite.patterns.memento.TransactionMemento;

public class DeleteCommand implements Command
{
    private DatabaseRepository repository;
    private Transaction transaction;
    private Context context;
    private TransactionMemento memento;

    public DeleteCommand(DatabaseRepository repository, Transaction transaction, Context context)
    {
        this.repository = repository;
        this.transaction = transaction;
        this.context = context;
    }

    @Override
    public void execute()
    {
        this.memento = new TransactionMemento(transaction);
        repository.deleteTransaction(context, transaction.getId());
    }

    @Override
    public void undo()
    {
        if(memento != null)
        {
            Transaction saved = memento.getSavedTransaction();
            repository.addData(context, saved);
        }
    }

}
