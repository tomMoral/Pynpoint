package com.huto.pynpoint.pynpoint;

import android.graphics.Rect;
import android.provider.BaseColumns;

/**
 * Created by tom on 25/04/15.
 */
public final class LocationRecord {
    public LocationRecord() {}

    /* Inner class that defines the table contents */
    public static abstract class Record implements BaseColumns {
        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
        public static final String COLUMN_NAME_ADDRESS = "address";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_TIME = "add_time";
    }

    public static final String TABLE_NAME = "record";
    public static final String COLUMN_NAME_NULLABLE = Record.COLUMN_NAME_ENTRY_ID;
    private static final String TEXT_TYPE = " TEXT";
    private static final String POSITION_TYPE = " FLOAT";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    Record._ID + " INTEGER PRIMARY KEY," +
                    Record.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    Record.COLUMN_NAME_ADDRESS + TEXT_TYPE + COMMA_SEP +
                    Record.COLUMN_NAME_LATITUDE + POSITION_TYPE + COMMA_SEP +
                    Record.COLUMN_NAME_LONGITUDE + POSITION_TYPE + COMMA_SEP +
                    Record.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                    Record.COLUMN_NAME_TIME + TEXT_TYPE +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
