package com.amisadman.aybaylite.Controllers;

import android.content.Context;
import com.amisadman.aybaylite.Repo.DatabaseHelper;

public class AuthFacade {
    private DatabaseHelper dbHelper;

    public AuthFacade(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public boolean login(String pin) {
        return dbHelper.checkPassword(pin);
    }

    public boolean register(String name, String email, String pin) {
        return dbHelper.insertData(name, email, pin);
    }

    public String getCurrentUser() {
        return dbHelper.getStoredName();
    }

    public boolean hasUser() {
        return dbHelper.getStoredName() != null;
    }
}