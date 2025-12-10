package com.amisadman.aybaylite.patterns.composite;

import java.util.ArrayList;
import java.util.List;

public class CategoryComposite implements TransactionComponent
{
    private String name;
    private List<TransactionComponent> components = new ArrayList<>();

    public CategoryComposite(String name)
    {
        this.name = name;
    }

    public void add(TransactionComponent component)
    {
        components.add(component);
    }

    public void remove(TransactionComponent component)
    {
        components.remove(component);
    }

    @Override
    public double getAmount()
    {
        double total = 0;
        for (TransactionComponent component : components)
        {
            total += component.getAmount();
        }
        return total;
    }

    @Override
    public String getDetails()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Category: ").append(name).append(" (Total: ").append(getAmount()).append(")\n");
        for (TransactionComponent component : components)
        {
            sb.append("  - ").append(component.getDetails()).append("\n");
        }
        return sb.toString();
    }
}
