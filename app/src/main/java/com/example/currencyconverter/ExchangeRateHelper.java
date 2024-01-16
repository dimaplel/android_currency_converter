package com.example.currencyconverter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class ExchangeRateHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ExchangeRates.db";

    public static final String EXCHANGE_RATES_TABLE = "rates";
    public static final String EXCHANGE_RATES_COL_CODE = "code";
    public static final String EXCHANGE_RATES_COL_RATE = "rate";
    private static final String SQL_CREATE_TABLE = "CREATE TABLE " + EXCHANGE_RATES_TABLE + " ("
            + BaseColumns._ID + " INTEGER PRIMARY KEY, "
            + EXCHANGE_RATES_COL_CODE + " TEXT UNIQUE,"
            + EXCHANGE_RATES_COL_RATE + " REAL)";
    private static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + EXCHANGE_RATES_TABLE;

    public ExchangeRateHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
    }
}
