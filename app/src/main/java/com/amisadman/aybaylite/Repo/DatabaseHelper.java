package com.amisadman.aybaylite.Repo;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
/*
    If you are using Cursor : close() korte vulben na
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper instance;
    private static Context appContext;

    private DatabaseHelper(Context context) {
        super(context.getApplicationContext(), "aybay", null, 1);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            appContext = context.getApplicationContext();
            instance = new DatabaseHelper(appContext);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table expense(id INTEGER primary key autoincrement,amount DOUBLE,reason TEXT,time INTEGER)");
        db.execSQL("create table income(id INTEGER primary key autoincrement,amount DOUBLE,reason TEXT,time INTEGER)");
        db.execSQL("create table loan(id INTEGER primary key autoincrement,amount DOUBLE,reason TEXT,time INTEGER)");
        db.execSQL("create table owe(id INTEGER primary key autoincrement,amount DOUBLE,reason TEXT,time INTEGER)");
        db.execSQL("create table savings(id INTEGER primary key autoincrement,amount DOUBLE,reason TEXT,time INTEGER)");
        db.execSQL("create table budget(id INTEGER primary key autoincrement,amount DOUBLE,reason TEXT,time INTEGER)");
        db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT UNIQUE, password TEXT)");

        // Chat Tables
        createChatTables(db);

    }

    private void createChatTables(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE chat_sessions(session_id TEXT PRIMARY KEY, title TEXT, last_updated INTEGER)");
        db.execSQL("CREATE TABLE chat_messages(id INTEGER PRIMARY KEY AUTOINCREMENT, session_id TEXT, message TEXT, sender TEXT, timestamp INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if(oldVersion < 2) { createChatTables(db); }
    }

    // ================= Chat Methods =================
    public void createSession(String sessionId, String title)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("session_id", sessionId);
        values.put("title", title);
        values.put("last_updated", System.currentTimeMillis());
        db.insert("chat_sessions", null, values);
    }

    public void addMessage(String sessionId, String message, String sender)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("session_id", sessionId);
        values.put("message", message);
        values.put("sender", sender);
        values.put("timestamp", System.currentTimeMillis());
        db.insert("chat_messages", null, values);

        // Update session timestamp
        ContentValues sessionValues = new ContentValues();
        sessionValues.put("last_updated", System.currentTimeMillis());
        db.update("chat_sessions", sessionValues, "session_id = ?", new String[] { sessionId });
    }

    public ArrayList<HashMap<String, String>> getChatHistory()
    {
        ArrayList<HashMap<String, String>> history = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM chat_sessions ORDER BY last_updated DESC", null);
        if (cursor.moveToFirst())
        {
            do
            {
                HashMap<String, String> session = new HashMap<>();
                session.put("session_id", cursor.getString(cursor.getColumnIndexOrThrow("session_id")));
                session.put("title", cursor.getString(cursor.getColumnIndexOrThrow("title")));
                session.put("last_updated", cursor.getString(cursor.getColumnIndexOrThrow("last_updated")));
                history.add(session);
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        return history;
    }

    public ArrayList<HashMap<String, String>> getMessages(String sessionId)
    {
        ArrayList<HashMap<String, String>> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM chat_messages WHERE session_id = ? ORDER BY timestamp ASC", new String[] { sessionId });
        if (cursor.moveToFirst())
        {
            do
            {
                HashMap<String, String> msg = new HashMap<>();
                msg.put("message", cursor.getString(cursor.getColumnIndexOrThrow("message")));
                msg.put("sender", cursor.getString(cursor.getColumnIndexOrThrow("sender")));
                messages.add(msg);
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        return messages;
    }

    public void deleteSession(String sessionId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("chat_sessions", "session_id = ?", new String[] { sessionId });
        db.delete("chat_messages", "session_id = ?", new String[] { sessionId });
    }

    // ====================================================================================

    public void addExpense(double amount, String reason, long time){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues conval = new ContentValues();
        conval.put("amount", amount);
        conval.put("reason", reason);
        conval.put("time", time == 0 ? System.currentTimeMillis() : time);

        db.insert("expense", null, conval);

    }

    public void addIncome(double amount, String reason, long time) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues conval = new ContentValues();
        conval.put("amount", amount);
        conval.put("reason", reason);
        conval.put("time", time == 0 ? System.currentTimeMillis() : time);

        db.insert("income", null, conval);

    }

    public void addLoan(double amount, String reason) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues conval = new ContentValues();
        conval.put("amount", amount);
        conval.put("reason", reason);
        conval.put("time", System.currentTimeMillis());

        db.insert("loan", null, conval);

    }

    public void addOwe(double amount, String reason) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues conval = new ContentValues();
        conval.put("amount", amount);
        conval.put("reason", reason);
        conval.put("time", System.currentTimeMillis());

        db.insert("owe", null, conval);

    }

    public void addSavings(double amount, String reason) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues conval = new ContentValues();
        conval.put("amount", amount);
        conval.put("reason", reason);
        conval.put("time", System.currentTimeMillis());

        db.insert("savings", null, conval);

    }

    public void addBudget(double amount, String reason) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues conval = new ContentValues();
        conval.put("amount", amount);
        conval.put("reason", reason);
        conval.put("time", System.currentTimeMillis());

        db.insert("budget", null, conval);

    }
    // ====================================================================================

    public double calculateTotalExpense() {
        double totalExpense = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from expense ORDER BY id DESC", null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                double amount = cursor.getDouble(1);
                totalExpense = totalExpense + amount;
            }
        }
        return totalExpense;
    }

    public double calculateTotalIncome() {
        double totalIncome = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from income ORDER BY id DESC", null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                double amount = cursor.getDouble(1);
                totalIncome = totalIncome + amount;
            }
        }
        return totalIncome;
    }

    public double calculateTotalLoan() {
        double totalLoan = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from loan ORDER BY id DESC", null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                double amount = cursor.getDouble(1);
                totalLoan = totalLoan + amount;
            }
        }
        return totalLoan;
    }

    public double calculateTotalOwe() {
        double totalOwe = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from owe ORDER BY id DESC", null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                double amount = cursor.getDouble(1);
                totalOwe = totalOwe + amount;
            }
        }
        return totalOwe;
    }

    public double calculateTotalSavings() {
        double totalSavings = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from savings ORDER BY id DESC", null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                double amount = cursor.getDouble(1);
                totalSavings = totalSavings + amount;
            }
        }
        return totalSavings;
    }

    public double calculateTotalBudget() {
        double totalBudget = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from budget ORDER BY id DESC", null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                double amount = cursor.getDouble(1);
                totalBudget = totalBudget + amount;
            }
        }
        return totalBudget;
    }
    // ====================================================================================

    public ArrayList<HashMap<String, String>> getAllExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from expense ORDER BY id DESC", null);
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        arrayList.clear();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                double amount = cursor.getDouble(1);
                String reason = cursor.getString(2);
                long timeMillis = cursor.getLong(3);
                String formattedTime = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date(timeMillis));

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("id", id);
                hashMap.put("amount", String.valueOf(amount));
                hashMap.put("reason", reason);
                hashMap.put("time", formattedTime);
                hashMap.put("timestamp", String.valueOf(timeMillis));

                arrayList.add(hashMap);
            }
            cursor.close();
        }
        return arrayList;
    }

    public ArrayList<HashMap<String, String>> getAllIncome() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from income ORDER BY id DESC", null);
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        arrayList.clear();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                double amount = cursor.getDouble(1);
                String reason = cursor.getString(2);
                long timeMillis = cursor.getLong(3);
                String formattedTime = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date(timeMillis));

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("id", id);
                hashMap.put("amount", String.valueOf(amount));
                hashMap.put("reason", reason);
                hashMap.put("time", formattedTime);
                hashMap.put("timestamp", String.valueOf(timeMillis));

                arrayList.add(hashMap);
            }
            cursor.close();
        }
        return arrayList;
    }

    public Cursor getAllOwe() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from owe ORDER BY id DESC", null);
        return cursor;
    }

    public Cursor getAllSavings() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from savings ORDER BY id DESC", null);
        return cursor;
    }

    public Cursor getAllBudget() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from budget ORDER BY id DESC", null);
        return cursor;
    }
    // ====================================================================================

    public void deleteItem(String tableName, String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + tableName + " WHERE id = ?", new String[] { id });
    }

    public boolean deleteExpense(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Use parameterized query with WHERE id = ?
            int rowsAffected = db.delete(
                    "expense", // Table name
                    "id = ?", // WHERE clause
                    new String[] { id } // WHERE value
            );

            Log.d("Database", "Deleted " + rowsAffected + " rows with id: " + id);
            return true;
        } catch (Exception e) {
            Log.e("Database", "Delete failed", e);
            return false;
        } finally {
            db.close();
        }
    }

    public boolean deleteIncome(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Use parameterized query with WHERE id = ?
            int rowsAffected = db.delete(
                    "income", // Table name
                    "id = ?", // WHERE clause
                    new String[] { id } // WHERE value
            );

            Log.d("Database", "Deleted " + rowsAffected + " rows with id: " + id);
            return true;
        } catch (Exception e) {
            Log.e("Database", "Delete failed", e);
            return false;
        } finally {
            db.close();
        }
    }

    public void deleteLoan(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from loan where id like " + id);
    }

    public void deleteOwe(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from owe where id like " + id);
    }

    public void deleteSavings(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from savings where id like " + id);
    }

    public void deleteBudget(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from budget where id like " + id);
    }

    // =====================================================================================
    public void updateExpense(String id, double amount, String reason, long time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("amount", amount);
        contentValues.put("reason", reason);
        db.update("expense", contentValues, "id = ?", new String[] { id });
    }

    public void updateIncome(String id, double amount, String reason, long time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("amount", amount);
        contentValues.put("reason", reason);
        db.update("income", contentValues, "id = ?", new String[] { id });
    }

    public void updateLoan(String id, double amount, String reason) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("amount", amount);
        contentValues.put("reason", reason);
        db.update("loan", contentValues, "id = ?", new String[] { id });
    }

    public void updateOwe(String id, double amount, String reason) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("amount", amount);
        contentValues.put("reason", reason);
        db.update("owe", contentValues, "id = ?", new String[] { id });
    }

    public void updateSavings(String id, double amount, String reason) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("amount", amount);
        contentValues.put("reason", reason);
        db.update("savings", contentValues, "id = ?", new String[] { id });
    }

    public void updateBudget(String id, double amount, String reason) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("amount", amount);
        contentValues.put("reason", reason);
        db.update("budget", contentValues, "id = ?", new String[] { id });
    }

    // =====================================================================================
    public Boolean insertData(String name, String email, String password) {
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT * FROM users", null);
        if (cursor.getCount() > 0) {
            cursor.close();
            return false; // Prevent multiple users
        }
        cursor.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("email", email);
        contentValues.put("password", password);

        long result = MyDatabase.insert("users", null, contentValues);
        return result != -1;
    }

    // Login r dashboard e name add kore
    public String getStoredName() {
        SQLiteDatabase MyDatabase = this.getReadableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT name FROM users LIMIT 1", null);

        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            cursor.close();
            return name;
        }

        cursor.close();
        return null; // No user exists
    }

    public Boolean checkPassword(String password) {
        SQLiteDatabase MyDatabase = this.getReadableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT * FROM users WHERE password = ?", new String[] { password });

        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        return isValid;
    }

    // Get Combined Statement (Income and Expenses) sorted by time
    public ArrayList<HashMap<String, String>> getStatement() {
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        arrayList.clear(); // Clear previous data

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 'Income' AS type, amount, reason, time FROM income " +
                "UNION ALL " +
                "SELECT 'Expense' AS type, -amount, reason, time FROM expense " +
                "ORDER BY time DESC", null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String type = cursor.getString(0);
                double amount = cursor.getDouble(1);
                String reason = cursor.getString(2);
                long timeMillis = cursor.getLong(3);
                String formattedTime = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss",
                        java.util.Locale.getDefault())
                        .format(new java.util.Date(timeMillis));

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("type", type);
                hashMap.put("amount", String.valueOf(amount));
                hashMap.put("reason", reason);
                hashMap.put("time", formattedTime);

                arrayList.add(hashMap);
            }
            cursor.close();
        }
        return arrayList;
    }

    /*
     * Prothome chilo rekhe disi
     * Jodi lage arki
     */
    public Boolean checkEmail(String email) {
        SQLiteDatabase MyDatabase = this.getReadableDatabase();
        Cursor cursor = MyDatabase.rawQuery("Select * from users where email = ?", new String[] { email });
        boolean exists = cursor.getCount() > 0;
        cursor.close(); // Close cursor to prevent memory leaks
        return exists;
    }

    public Boolean checkEmailPassword(String email, String password) {
        SQLiteDatabase MyDatabase = this.getReadableDatabase();
        Cursor cursor = MyDatabase.rawQuery("Select * from users where email = ? and password = ?",
                new String[] { email, password });
        boolean exists = cursor.getCount() > 0;
        cursor.close(); // Close cursor to prevent memory leaks
        return exists;
    }

    // ========================================================================

    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> search(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<HashMap<String, String>> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT 'expense' AS tableName, id, amount, reason, time FROM expense WHERE reason LIKE ? UNION " +
                            "SELECT 'income' AS tableName, id, amount, reason, time FROM income WHERE reason LIKE ? UNION "
                            +
                            "SELECT 'loan' AS tableName, id, amount, reason, time FROM loan WHERE reason LIKE ? UNION "
                            +
                            "SELECT 'owe' AS tableName, id, amount, reason, time FROM owe WHERE reason LIKE ? UNION " +
                            "SELECT 'savings' AS tableName, id, amount, reason, time FROM savings WHERE reason LIKE ? UNION "
                            +
                            "SELECT 'budget' AS tableName, id, amount, reason, time FROM budget WHERE reason LIKE ?",
                    new String[] { "%" + query + "%", "%" + query + "%", "%" + query + "%",
                            "%" + query + "%", "%" + query + "%", "%" + query + "%" });

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("tableName", cursor.getString(cursor.getColumnIndexOrThrow("tableName")));
                    map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                    map.put("amount", cursor.getString(cursor.getColumnIndexOrThrow("amount")));
                    map.put("reason", cursor.getString(cursor.getColumnIndexOrThrow("reason")));
                    map.put("time", cursor.getString(cursor.getColumnIndexOrThrow("time")));
                    results.add(map);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace(); // log error if something goes wrong
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return results;
    }

}
