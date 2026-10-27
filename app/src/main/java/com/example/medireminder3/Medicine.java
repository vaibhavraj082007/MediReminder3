package com.example.medireminder3;

public class Medicine {
    public int id;
    public String name;
    public int doseQty;       // tablets per dose
    public int timesPerDay;   // doses per day
    public int stock;         // tablets left
    public long expiryMillis; // expiry date as a number
    public int hour, minute;  // first reminder time

    public Medicine(int id, String name, int doseQty, int timesPerDay,
                    int stock, long expiryMillis, int hour, int minute) {
        this.id = id;
        this.name = name;
        this.doseQty = doseQty;
        this.timesPerDay = timesPerDay;
        this.stock = stock;
        this.expiryMillis = expiryMillis;
        this.hour = hour;
        this.minute = minute;
    }
}
