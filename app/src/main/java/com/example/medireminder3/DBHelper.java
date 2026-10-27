package com.example.medireminder3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "medi.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE medicines (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, doseQty INTEGER, timesPerDay INTEGER, " +
                "stock INTEGER, expiryMillis INTEGER, " +
                "hour INTEGER, minute INTEGER)");

        db.execSQL("CREATE TABLE dose_log (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "medicineId INTEGER, timeMillis INTEGER, status TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS medicines");
        db.execSQL("DROP TABLE IF EXISTS dose_log");
        onCreate(db);
    }

    public long addMedicine(Medicine m) {
        ContentValues cv = new ContentValues();
        cv.put("name", m.name);
        cv.put("doseQty", m.doseQty);
        cv.put("timesPerDay", m.timesPerDay);
        cv.put("stock", m.stock);
        cv.put("expiryMillis", m.expiryMillis);
        cv.put("hour", m.hour);
        cv.put("minute", m.minute);
        return getWritableDatabase().insert("medicines", null, cv);
    }

    public ArrayList<Medicine> getAllMedicines() {
        ArrayList<Medicine> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM medicines ORDER BY name", null);
        while (c.moveToNext()) {
            list.add(new Medicine(
                    c.getInt(0), c.getString(1), c.getInt(2), c.getInt(3),
                    c.getInt(4), c.getLong(5), c.getInt(6), c.getInt(7)));
        }
        c.close();
        return list;
    }

    public void reduceStock(int id, int qty) {
        getWritableDatabase().execSQL(
                "UPDATE medicines SET stock = MAX(stock - ?, 0) WHERE id = ?",
                new Object[]{qty, id});
    }

    public void logDose(int medicineId, String status) {
        ContentValues cv = new ContentValues();
        cv.put("medicineId", medicineId);
        cv.put("timeMillis", System.currentTimeMillis());
        cv.put("status", status);
        getWritableDatabase().insert("dose_log", null, cv);
    }

    public int countDoses(String status) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM dose_log WHERE status = ?",
                new String[]{status});
        c.moveToFirst();
        int n = c.getInt(0);
        c.close();
        return n;
    }

    public void deleteMedicine(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("medicines", "id=?", new String[]{String.valueOf(id)});
        db.delete("dose_log", "medicineId=?", new String[]{String.valueOf(id)});
    }
}
