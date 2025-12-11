package com.amisadman.aybaylite.Repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

public class RestoreManager {
    private Context context;
    private DatabaseHelper dbHelper;

    public RestoreManager(Context context) {
        this.context = context;
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public boolean restoreFromJson(Uri uri) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            String jsonString = readTextFromUri(uri);
            JSONObject backupJson = new JSONObject(jsonString);

            // Clear existing data before restore ?? Or just append?
            // Usually restore overwrites or appends. Let's clear for safety if it's a full
            // restore.
            // But user might want to merge.
            // Given "Restore" usually means "Bring back state", let's clear tables first to
            // avoid duplicates if IDs collide.

            clearTables(db);

            restoreTable(db, backupJson, "expense");
            restoreTable(db, backupJson, "income");
            restoreTable(db, backupJson, "loan");
            restoreTable(db, backupJson, "owe");
            restoreTable(db, backupJson, "savings");
            restoreTable(db, backupJson, "budget");
            restoreTable(db, backupJson, "chat_sessions");
            restoreTable(db, backupJson, "chat_messages");

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    private void clearTables(SQLiteDatabase db) {
        db.execSQL("DELETE FROM expense");
        db.execSQL("DELETE FROM income");
        db.execSQL("DELETE FROM loan");
        db.execSQL("DELETE FROM owe");
        db.execSQL("DELETE FROM savings");
        db.execSQL("DELETE FROM budget");
        db.execSQL("DELETE FROM chat_sessions");
        db.execSQL("DELETE FROM chat_messages");
    }

    private void restoreTable(SQLiteDatabase db, JSONObject backupJson, String tableName) throws JSONException {
        if (backupJson.has(tableName)) {
            JSONArray tableData = backupJson.getJSONArray(tableName);
            for (int i = 0; i < tableData.length(); i++) {
                JSONObject row = tableData.getJSONObject(i);
                ContentValues values = new ContentValues();
                Iterator<String> keys = row.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    // Don't import 'id' if autoincrement is preferred, but for full restore we
                    // usually keep IDs.
                    // However, if we clear tables, we can keep IDs.
                    values.put(key, row.getString(key));
                }
                db.insert(tableName, null, values);
            }
        }
    }

    private String readTextFromUri(Uri uri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        inputStream.close();
        return stringBuilder.toString();
    }
}
