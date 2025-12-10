package com.amisadman.aybaylite.patterns.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateAdapter {
    private final SimpleDateFormat dateFormat;

    public DateAdapter() {
        this.dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault());
    }

    public String format(long timestamp) {
        if (timestamp == 0)
            return "";
        return dateFormat.format(new Date(timestamp));
    }
}
