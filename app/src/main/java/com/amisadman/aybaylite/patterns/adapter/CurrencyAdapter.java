package com.amisadman.aybaylite.patterns.adapter;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyAdapter implements CurrencyFormatter
{
    private NumberFormat numberFormat;

    public CurrencyAdapter()
    {
        this.numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "BD"));
    }

    @Override
    public String format(double amount)
    {
        String formatted = numberFormat.format(amount);
        if (!formatted.contains("৳"))
        {
            return "৳" + formatted.replace(numberFormat.getCurrency().getSymbol(), "");
        }
        return formatted;
    }

}
