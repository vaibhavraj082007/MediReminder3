package com.example.medireminder3;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class AddMedicineActivity extends AppCompatActivity {

    EditText etName, etDose, etTimes, etStock;
    Button btnExpiry, btnTime, btnSave;
    long expiryMillis = 0;
    int[] hours = new int[0];
    int[] minutes = new int[0];
    int pickedFor = 0;   // how many times were picked
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        // BACK BUTTON
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etName = findViewById(R.id.etName);
        etDose = findViewById(R.id.etDose);
        etTimes = findViewById(R.id.etTimes);
        etStock = findViewById(R.id.etStock);
        btnExpiry = findViewById(R.id.btnExpiry);
        btnTime = findViewById(R.id.btnTime);
        btnSave = findViewById(R.id.btnSave);
        db = new DBHelper(this);

        btnTime.setText("Pick Reminder Times");

        // DATE PICKER: expiry date
        btnExpiry.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(y, m, d, 23, 59, 59);
                expiryMillis = chosen.getTimeInMillis();
                btnExpiry.setText("Expiry: " + d + "/" + (m + 1) + "/" + y);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // TIME PICKER: one time for every dose in the day
        btnTime.setOnClickListener(v -> startPickingTimes());

        btnSave.setOnClickListener(v -> saveMedicine());
    }

    private void startPickingTimes() {
        String times = etTimes.getText().toString().trim();
        if (times.isEmpty()) {
            Toast.makeText(this, "First enter times per day", Toast.LENGTH_SHORT).show();
            return;
        }
        int n = Integer.parseInt(times);
        if (n < 1 || n > 10) {
            Toast.makeText(this, "Times per day must be 1 to 10", Toast.LENGTH_SHORT).show();
            return;
        }
        hours = new int[n];
        minutes = new int[n];
        pickedFor = 0;
        askTime(0, n);
    }

    private void askTime(int index, int n) {
        TimePickerDialog dialog = new TimePickerDialog(this, (view, h, min) -> {
            hours[index] = h;
            minutes[index] = min;
            if (index + 1 < n) {
                askTime(index + 1, n);
            } else {
                pickedFor = n;
                StringBuilder sb = new StringBuilder("Times: ");
                for (int i = 0; i < n; i++) {
                    if (i > 0) sb.append(", ");
                    int h12 = hours[i] % 12 == 0 ? 12 : hours[i] % 12;
                    sb.append(String.format("%02d:%02d %s", h12, minutes[i], hours[i] < 12 ? "AM" : "PM"));}
                btnTime.setText(sb.toString());
            }
        }, 8, 0, false);
        dialog.setTitle("Dose " + (index + 1) + " of " + n);
        dialog.show();
    }

    private void saveMedicine() {
        String name = etName.getText().toString().trim();
        String dose = etDose.getText().toString().trim();
        String times = etTimes.getText().toString().trim();
        String stock = etStock.getText().toString().trim();

        if (name.isEmpty() || dose.isEmpty() || times.isEmpty() || stock.isEmpty()
                || expiryMillis == 0) {
            Toast.makeText(this, "Please fill everything and pick the expiry date",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int doseQty = Integer.parseInt(dose);
        int timesPerDay = Integer.parseInt(times);
        int stockQty = Integer.parseInt(stock);

        if (doseQty < 1 || timesPerDay < 1) {
            Toast.makeText(this, "Dose and times must be at least 1", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pickedFor != timesPerDay) {
            Toast.makeText(this, "Please pick all " + timesPerDay + " reminder times",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Medicine m = new Medicine(0, name, doseQty, timesPerDay, stockQty,
                expiryMillis, hours[0], minutes[0]);

        int newId = (int) db.addMedicine(m);
        m.id = newId;
        AlarmHelper.setReminders(this, m, hours, minutes);

        Toast.makeText(this, "Medicine saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}