package com.eeseetech.nagrand.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dong on 2017/4/10.
 */

public class DataBaseHelper extends SQLiteOpenHelper {

    private static DataBaseHelper mInstance;

    public static final String DB_NAME = "Nagrand.db";
    public static final int DB_VERSION = 1;
    public static final String VIDEO_TABLE_NAME = "videos";
    public static final String HISTORY_TABLE_NAME = "history";

    private static final String CREATE_VIDEO_TABLE = "create table " + VIDEO_TABLE_NAME + "(name text primary key,id text,time_period text,md5sum text)";

    private static final String CREATE_HISTORY_TABLE = "create table history (time text primary key,name text)";

    private DataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public synchronized static DataBaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DataBaseHelper(context);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_VIDEO_TABLE);
        db.execSQL(CREATE_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
