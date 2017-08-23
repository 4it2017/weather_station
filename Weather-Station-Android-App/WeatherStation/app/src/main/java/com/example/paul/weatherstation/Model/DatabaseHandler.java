package com.example.paul.weatherstation.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul on 07-Aug-17.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "weatherManager";

    // WeatherRecordings table name
    private static final String TABLE_WEATHER_RECORDS = "weatherRecordings";

    // WeatherRecordings Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TIME = "time";
    private static final String KEY_TEMP = "temperature";
    private static final String KEY_HUM = "humidity";
    private static final String KEY_PRESS = "pressure";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_WEATHER_RECORDS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIME + " TEXT,"
                + KEY_TEMP + " TEXT," + KEY_HUM + " TEXT," + KEY_PRESS + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEATHER_RECORDS);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new weather recording
    public void addWeatherRecord(WeatherRecord weatherRecord) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIME, weatherRecord.getTime());
        values.put(KEY_TEMP, weatherRecord.getTemperature());
        values.put(KEY_HUM, weatherRecord.getHumidity());
        values.put(KEY_PRESS, weatherRecord.getPressure());

        // Inserting Row
        db.insert(TABLE_WEATHER_RECORDS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single WeatherRecord
    public WeatherRecord getWeatherRecord(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_WEATHER_RECORDS, new String[]{KEY_ID,
                        KEY_TIME, KEY_TEMP, KEY_HUM, KEY_PRESS}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        WeatherRecord weatherRecord = null;
        if (cursor != null) {
            weatherRecord = new WeatherRecord(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
        }
        // return contact
        return weatherRecord;
    }

    //Get Last Entry
    public WeatherRecord getLastWeatherRecord(){
        WeatherRecord weatherRecord = new WeatherRecord();
        String selectQuery = "SELECT * FROM " + TABLE_WEATHER_RECORDS;

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToLast()){
            weatherRecord.setID(Integer.parseInt(cursor.getString(0)));
            weatherRecord.setTime(cursor.getString(1));
            weatherRecord.setTemperature(cursor.getString(2));
            weatherRecord.setHumidity(cursor.getString(3));
            weatherRecord.setPressure(cursor.getString(4));
            return weatherRecord;
        } else {
          return null;
        }
    }


    // Getting All WeatherRecordings
    public List<WeatherRecord> getAllWeatherRecords() {
        List<WeatherRecord> weatherRecordingsList = new ArrayList<WeatherRecord>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_WEATHER_RECORDS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                WeatherRecord weatherRecord = new WeatherRecord();
                weatherRecord.setID(Integer.parseInt(cursor.getString(0)));
                weatherRecord.setTime(cursor.getString(1));
                weatherRecord.setTemperature(cursor.getString(2));
                weatherRecord.setHumidity(cursor.getString(3));
                weatherRecord.setPressure(cursor.getString(4));
                // Adding weatherRecord to list
                weatherRecordingsList.add(weatherRecord);
            } while (cursor.moveToNext());
        }

        // return weatherRecords list
        return weatherRecordingsList;
    }

    // Updating single weatherRecord
    public int updateWeatherRecord(WeatherRecord weatherRecord) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIME, weatherRecord.getTime());
        values.put(KEY_TEMP, weatherRecord.getTemperature());
        values.put(KEY_HUM, weatherRecord.getHumidity());
        values.put(KEY_PRESS, weatherRecord.getPressure());

        // updating row
        return db.update(TABLE_WEATHER_RECORDS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(weatherRecord.getID()) });
    }

    // Deleting single WeatherRecord
    public void deleteWeatherRecord(WeatherRecord weatherRecord) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WEATHER_RECORDS, KEY_ID + " = ?",
                new String[] { String.valueOf(weatherRecord.getID()) });
        db.close();
    }

    // Getting WeatherRecords Total Count
    public int getWeatherRecordsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_WEATHER_RECORDS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

}
