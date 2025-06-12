package com.example.wattnow;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "calculationDb";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "calculations";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creates the 'calculations' table with appropriate columns
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "month TEXT," +
                "unit REAL," +
                "rebate INTEGER," +
                "totalCharges REAL," +
                "finalCost REAL," +
                "rebateSavings REAL)";
        db.execSQL(createTable);
    }

    // Inserts a new bil calculation record into the database
    public void insertCalculation(String month, double unit, int rebate, double totalCharges, double finalCost, double rebateSavings) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("month", month);
        values.put("unit", unit);
        values.put("rebate", rebate);
        values.put("totalCharges", totalCharges);
        values.put("finalCost", finalCost);
        values.put("rebateSavings", rebateSavings);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // Retrieve all bill records from the database
    public Cursor getAllBills() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY id DESC", null);
    }

    // Retrieve a single bill record by its unique ID
    public Cursor getBillById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE id = ?", new String[]{String.valueOf(id)});
    }

    // Updates an existing bill record identification by its ID
    public boolean updateBill(int id, String month, double unit, int rebate, double totalCharges, double finalCost, double rebateSavings) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("month", month);
        values.put("unit", unit);
        values.put("rebate", rebate);
        values.put("totalCharges", totalCharges);
        values.put("finalCost", finalCost);
        values.put("rebateSavings", rebateSavings);

        int rowsUpdated = db.update(TABLE_NAME, values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsUpdated > 0;
    }

    // Delete a bill record from the database using its ID
    public boolean deleteBillById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(TABLE_NAME, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return deletedRows > 0;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
