package com.huto.pynpoint.pynpoint;

/**
 * Created by tom on 25/04/15.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DbPynpoint extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Pynpoint.db";


    public DbPynpoint(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LocationRecord.SQL_CREATE_ENTRIES);
        //db.close();
    }

    public  void resetTable(SQLiteDatabase db){
        db.execSQL(LocationRecord.SQL_DELETE_ENTRIES);
        db.execSQL(LocationRecord.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //THIS WILL BE EXECUTED WHEN YOU UPDATED VERSION OF DATABASE_VERSION
        //YOUR DROP AND CREATE QUERIES
    }
}