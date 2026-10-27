package com.example.medireminder3;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

// Reads the History calendar data from the MAIN database (medi.db, table dose_log)
public class DoseLogStore {

    private final DBHelper helper;

    public DoseLogStore(Context ctx) {
        helper = new DBHelper(ctx);
    }

    // Not needed anymore: DBHelper.logDose() already saves every dose.
    // Kept so old calls like logStore.add(...) still compile.
    public void add(String name, String status) {
    }

    public int countAll(String status) {
        try {
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor c = db.rawQuery(
                    "SELECT COUNT(*) FROM dose_log WHERE status=?", new String[]{status});
            int n = 0;
            if (c.moveToFirst()) n = c.getInt(0);
            c.close();
            return n;
        } catch (Exception e) {
            return 0;
        }
    }

    public int countDay(String day, String status) {
        try {
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor c = db.rawQuery(
                    "SELECT COUNT(*) FROM dose_log WHERE status=? AND "
                            + "strftime('%Y-%m-%d', timeMillis/1000, 'unixepoch', 'localtime')=?",
                    new String[]{status, day});
            int n = 0;
            if (c.moveToFirst()) n = c.getInt(0);
            c.close();
            return n;
        } catch (Exception e) {
            return 0;
        }
    }

    // each row looks like: "08:15 AM   Paracetamol  -  ✔ Taken"
    public ArrayList<String> forDay(String day) {
        ArrayList<String> out = new ArrayList<>();
        try {
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor c = db.rawQuery(
                    "SELECT m.name, d.status, d.timeMillis "
                            + "FROM dose_log d LEFT JOIN medicines m ON m.id = d.medicineId "
                            + "WHERE strftime('%Y-%m-%d', d.timeMillis/1000, 'unixepoch', 'localtime')=? "
                            + "ORDER BY d.timeMillis",
                    new String[]{day});
            SimpleDateFormat f = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            while (c.moveToNext()) {
                String name = c.getString(0);
                if (name == null) name = "Deleted medicine";
                String st = "taken".equals(c.getString(1)) ? "✔ Taken" : "✖ Missed";
                out.add(f.format(new Date(c.getLong(2))) + "   " + name + "  -  " + st);
            }
            c.close();
        } catch (Exception e) {
            // leave the list empty
        }
        return out;
    }
}