package com.example.medireminder3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listMedicines;
    TextView tvSummary;
    DBHelper db;
    MedicineAdapter adapter;
    ArrayList<Medicine> meds;

    android.os.Handler refresher = new android.os.Handler(android.os.Looper.getMainLooper());
    Runnable refreshTask = new Runnable() {
        @Override
        public void run() {
            if (adapter != null) adapter.notifyDataSetChanged();
            refresher.postDelayed(this, 30000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listMedicines = findViewById(R.id.listMedicines);
        tvSummary = findViewById(R.id.tvSummary);
        db = new DBHelper(this);

        // Android 13+ needs permission to show notifications
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        findViewById(R.id.btnAdd).setOnClickListener(v ->
                startActivity(new Intent(this, AddMedicineActivity.class)));

        findViewById(R.id.btnFamily).setOnClickListener(v ->
                startActivity(new Intent(this, CaregiverActivity.class)));

        // History calendar
        findViewById(R.id.btnCalendar).setOnClickListener(v ->
                startActivity(new Intent(this, CalendarActivity.class)));

        // Share my user code so a family member can watch my doses
        findViewById(R.id.btnCode).setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, uid);
            startActivity(Intent.createChooser(share, "Share my code"));
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    // Runs every time we come back to this screen, so the list refreshes
    @Override
    protected void onResume() {
        super.onResume();
        meds = db.getAllMedicines();
        adapter = new MedicineAdapter(this, meds, this::updateSummary);
        listMedicines.setAdapter(adapter);
        refresher.removeCallbacks(refreshTask);
        refresher.postDelayed(refreshTask, 30000);
        updateSummary();
        checkLowStock();
    }

    @Override
    protected void onPause() {
        super.onPause();
        refresher.removeCallbacks(refreshTask);
    }

    // Red alert on open for any medicine that is low or out of stock
    private void checkLowStock() {
        StringBuilder sb = new StringBuilder();
        for (Medicine m : meds) {
            int perDay = Math.max(1, m.doseQty * m.timesPerDay);
            int daysLeft = m.stock / perDay;
            if (m.stock <= 0) {
                sb.append(m.name).append(": OUT of stock\n");
            } else if (daysLeft <= 3) {
                sb.append(m.name).append(": only ").append(m.stock)
                        .append(" left (").append(daysLeft).append(" days)\n");
            }
        }
        if (sb.length() > 0) {
            AlertHelper.redToast(this, "Low stock\n" + sb.toString().trim());
        }
    }

    // Header summary and adherence score
    private void updateSummary() {
        int taken = db.countDoses("taken");
        int missed = db.countDoses("missed");
        int count = meds.size();
        if (taken + missed == 0) {
            tvSummary.setText(count + " medicine(s) added  |  Tap Taken after each dose");
        } else {
            int score = (taken * 100) / (taken + missed);
            tvSummary.setText(count + " medicine(s)  |  Adherence: " + score + "%  ("
                    + taken + " taken, " + missed + " missed)");
        }
    }
}