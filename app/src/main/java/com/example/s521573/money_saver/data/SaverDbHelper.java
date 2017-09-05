package com.example.s521573.money_saver.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


class SaverDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "saver.db";
    private static final int DATABASE_VERSION = 1;

    public SaverDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_SAVER_TABLE = "CREATE TABLE " + SaverContract.SaverEntry.TABLE_NAME + "(" +
                SaverContract.SaverEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SaverContract.SaverEntry.COLUMN_IMPORTANCE + " INTEGER NOT NULL, " +
                SaverContract.SaverEntry.COLUMN_PRODUCT + " TEXT NOT NULL, " +
                SaverContract.SaverEntry.COLUMN_PRICE + " INTEGER NOT NULL, " +
                SaverContract.SaverEntry.COLUMN_CATEGORY + " INTEGER, " +
                SaverContract.SaverEntry.COLUMN_CURRENT_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

        db.execSQL(SQL_SAVER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SaverContract.SaverEntry.TABLE_NAME);
        onCreate(db);
    }
}
