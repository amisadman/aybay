package com.amisadman.aybaylite.Repo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BackupManager {
    private DatabaseHelper dbHelper;

    public BackupManager(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public String getBackupJson() {
        JSONObject backupJson = new JSONObject();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            backupJson.put("expense", getTableData(db, "expense"));
            backupJson.put("income", getTableData(db, "income"));
            backupJson.put("loan", getTableData(db, "loan"));
            backupJson.put("owe", getTableData(db, "owe"));
            backupJson.put("savings", getTableData(db, "savings"));
            backupJson.put("budget", getTableData(db, "budget"));
            backupJson.put("chat_sessions", getTableData(db, "chat_sessions"));
            backupJson.put("chat_messages", getTableData(db, "chat_messages"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return backupJson.toString();
    }

    private JSONArray getTableData(SQLiteDatabase db, String tableName) {
        JSONArray tableData = new JSONArray();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    JSONObject row = new JSONObject();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        String columnName = cursor.getColumnName(i);
                        String value = cursor.getString(i);
                        try {
                            row.put(columnName, value);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    tableData.put(row);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return tableData;
    }
}
