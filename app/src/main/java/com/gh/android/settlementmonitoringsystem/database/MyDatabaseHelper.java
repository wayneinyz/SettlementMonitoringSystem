package com.gh.android.settlementmonitoringsystem.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "database.db";
    private static final String TABLE_CDKEY = "cdkey";

    private static final String CREATE_TABLE_CDKEY = "create table "
            + TABLE_CDKEY
            + " ("
            + "cdkey integer primary key autoincrement"
            + ");";

    public MyDatabaseHelper(Context context, int version) {
        super(context, DB_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CDKEY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
