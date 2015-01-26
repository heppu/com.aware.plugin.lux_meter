package com.aware.plugin.lux_meter;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

public class Provider extends ContentProvider {

    public static final int DATABASE_VERSION = 1;

    /**
     * Provider authority: com.aware.plugin.lux_meter.provider.lux_meter
     */

    public static String AUTHORITY = "com.aware.plugin.lux_meter.provider.lux_meter";

    private static final int LUX_METER = 1;
    private static final int LUX_METER_ID = 2;

    public static final String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/plugin_lux_meter.db";

    public static final String[] DATABASE_TABLES = {
            "plugin_lux_meter"
    };

    public static final String[] TABLES_FIELDS = {
            LuxMeter_Data._ID + " integer primary key autoincrement," +
                    LuxMeter_Data.TIMESTAMP + " real default 0," +
                    LuxMeter_Data.DEVICE_ID + " text default ''," +
                    LuxMeter_Data.UPDATE_FREQUENCY + " real default 0," +
                    LuxMeter_Data.LIGHT_THRESHOLD + " real default 0," +
                    LuxMeter_Data.LUX_AVG + " integer default 0," +
                    LuxMeter_Data.OVER_THRESHOLD + " integer default 0," +
                    "UNIQUE("+LuxMeter_Data.TIMESTAMP+","+LuxMeter_Data.DEVICE_ID+")"
    };

    public static final class LuxMeter_Data implements BaseColumns {
        private LuxMeter_Data(){};

        public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/plugin_lux_meter");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.lux_meter";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.lux_meter";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String UPDATE_FREQUENCY = "double_frequency";
        public static final String OVER_THRESHOLD = "over_threshold";
        public static final String LUX_AVG = "lux_avg";
        public static final String LIGHT_THRESHOLD = "light_threshold";
    }

    private static UriMatcher URIMatcher;
    private static HashMap<String, String> databaseMap;
    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase database;

    @Override
    public boolean onCreate() {

        AUTHORITY = getContext().getPackageName() + ".provider.lux_meter";

        URIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], LUX_METER);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0]+"/#", LUX_METER_ID);

        databaseMap = new HashMap<String, String>();
        databaseMap.put(LuxMeter_Data._ID, LuxMeter_Data._ID);
        databaseMap.put(LuxMeter_Data.TIMESTAMP, LuxMeter_Data.TIMESTAMP);
        databaseMap.put(LuxMeter_Data.DEVICE_ID, LuxMeter_Data.DEVICE_ID);
        databaseMap.put(LuxMeter_Data.UPDATE_FREQUENCY, LuxMeter_Data.UPDATE_FREQUENCY);
        databaseMap.put(LuxMeter_Data.OVER_THRESHOLD, LuxMeter_Data.OVER_THRESHOLD);
        databaseMap.put(LuxMeter_Data.LUX_AVG, LuxMeter_Data.LUX_AVG);
        databaseMap.put(LuxMeter_Data.LIGHT_THRESHOLD, LuxMeter_Data.LIGHT_THRESHOLD);

        return true;
    }

    private boolean initializeDB() {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper( getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS );
        }
        if( databaseHelper != null && ( database == null || ! database.isOpen() )) {
            database = databaseHelper.getWritableDatabase();
        }
        return( database != null && databaseHelper != null);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (URIMatcher.match(uri)) {
            case LUX_METER:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (URIMatcher.match(uri)) {
            case LUX_METER:
                return LuxMeter_Data.CONTENT_TYPE;
            case LUX_METER_ID:
                return LuxMeter_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        switch (URIMatcher.match(uri)) {
            case LUX_METER:
                long weather_id = database.insert(DATABASE_TABLES[0], LuxMeter_Data.DEVICE_ID, values);

                if (weather_id > 0) {
                    Uri new_uri = ContentUris.withAppendedId(
                            LuxMeter_Data.CONTENT_URI,
                            weather_id);
                    getContext().getContentResolver().notifyChange(new_uri,
                            null);
                    return new_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (URIMatcher.match(uri)) {
            case LUX_METER:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(databaseMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());

            return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (URIMatcher.match(uri)) {
            case LUX_METER:
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                break;
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
