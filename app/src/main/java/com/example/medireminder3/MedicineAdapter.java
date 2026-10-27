package com.example.medireminder3;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MedicineAdapter extends ArrayAdapter<Medicine> {

    static final long DAY = 24L * 60 * 60 * 1000;
    static final long LOCK_MILLIS = 2L * 60 * 60 * 1000;   // 2 hours
    DBHelper db;
    DoseLogStore logStore;
    Runnable onChange;

    public MedicineAdapter(Context context, ArrayList<Medicine> list, Runnable onChange) {
        super(context, 0, list);
        this.db = new DBHelper(context);
        this.logStore = new DoseLogStore(context);
        this.onChange = onChange;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_medicine, parent, false);
        }

        Medicine med = getItem(position);

        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvDose = convertView.findViewById(R.id.tvDose);
        TextView tvStock = convertView.findViewById(R.id.tvStock);
        TextView tvExpiry = convertView.findViewById(R.id.tvExpiry);
        TextView tvWarning = convertView.findViewById(R.id.tvWarning);
        Button btnTaken = convertView.findViewById(R.id.btnTaken);
        Button btnSkip = convertView.findViewById(R.id.btnSkip);
        Button btnDelete = convertView.findViewById(R.id.btnDelete);

        // ---------- SMART LOGIC ----------
        long now = System.currentTimeMillis();
        int tabletsPerDay = Math.max(1, med.doseQty * med.timesPerDay);
        int daysLeft = med.stock / tabletsPerDay;
        long daysToExpiry = (med.expiryMillis - now) / DAY;
        long stockEndMillis = now + daysLeft * DAY;

        String warning = "";
        if (med.expiryMillis < now) {
            warning = "EXPIRED - do not use";
        } else if (med.stock == 0) {
            warning = "Out of stock - refill now";
        } else if (med.expiryMillis < stockEndMillis) {
            warning = "Expires before your stock finishes";
        }

        int color;
        if (med.expiryMillis < now)  color = Color.parseColor("#BDBDBD");
        else if (daysLeft <= 3)      color = Color.parseColor("#FFCDD2");
        else if (daysToExpiry < 30)  color = Color.parseColor("#FFE0B2");
        else                         color = Color.parseColor("#C8E6C9");

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(40f);
        convertView.findViewById(R.id.cardRoot).setBackground(bg);

        // ---------- SHOW DATA ----------
        String expiryText = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(new Date(med.expiryMillis));

        tvName.setText(med.name);
        tvDose.setText(med.doseQty + " tablet(s), " + med.timesPerDay + " time(s) a day");
        tvStock.setText(med.stock + " left - lasts " + daysLeft + " days");
        tvExpiry.setText("Expiry: " + expiryText);
        if (warning.isEmpty()) {
            tvWarning.setVisibility(View.GONE);
        } else {
            tvWarning.setText(warning);
            tvWarning.setVisibility(View.VISIBLE);
        }

        // ---------- TAKEN LOCK (2 hours) ----------
        SharedPreferences sp =
                getContext().getSharedPreferences("medi_prefs", Context.MODE_PRIVATE);
        long lastTaken = sp.getLong("last_taken_" + med.id, 0);
        long unlockAt = lastTaken + LOCK_MILLIS;

        if (lastTaken != 0 && now < unlockAt) {
            long mins = (unlockAt - now) / 60000 + 1;
            btnTaken.setEnabled(false);
            btnTaken.setAlpha(0.4f);
            btnTaken.setText("Locked " + (mins >= 60 ? (mins / 60) + "h " : "") + (mins % 60) + "m");
        } else {
            btnTaken.setEnabled(true);
            btnTaken.setAlpha(1f);
            btnTaken.setText("Taken");
        }

        // ---------- BUTTONS ----------
        btnTaken.setOnClickListener(v -> {
            long last = sp.getLong("last_taken_" + med.id, 0);
            if (last != 0 && System.currentTimeMillis() < last + LOCK_MILLIS) {
                Toast.makeText(getContext(), "Taken is locked for 2 hours after each dose",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (med.expiryMillis < System.currentTimeMillis()) {
                Toast.makeText(getContext(), "This medicine is expired", Toast.LENGTH_SHORT).show();
                return;
            }
            if (med.stock <= 0) {
                Toast.makeText(getContext(), "No stock left", Toast.LENGTH_SHORT).show();
                return;
            }

            // save the lock time FIRST
            sp.edit().putLong("last_taken_" + med.id, System.currentTimeMillis()).apply();

            db.reduceStock(med.id, med.doseQty);
            db.logDose(med.id, "taken");
            logStore.add(med.name, "taken");          // <-- feeds the History calendar
            med.stock = Math.max(0, med.stock - med.doseQty);

            String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
            sendStatus(med.name, "Taken at " + time);

            int perDay = Math.max(1, med.doseQty * med.timesPerDay);
            if (med.stock == 0) {
                AlertHelper.redToast(getContext(), med.name + " is OUT of stock. Refill now!");
            } else if (med.stock / perDay <= 3) {
                AlertHelper.redToast(getContext(), "Low stock: " + med.name + " has only "
                        + med.stock + " left (" + (med.stock / perDay) + " days).");
            }

            notifyDataSetChanged();
            onChange.run();
        });

        btnSkip.setOnClickListener(v -> {
            db.logDose(med.id, "missed");
            logStore.add(med.name, "missed");         // <-- feeds the History calendar
            sendStatus(med.name, "Missed / skipped");
            Toast.makeText(getContext(), "Marked as missed", Toast.LENGTH_SHORT).show();
            onChange.run();
        });

        btnDelete.setOnClickListener(v -> {
            db.deleteMedicine(med.id);
            AlarmHelper.cancelReminder(getContext(), med.id);
            sp.edit().remove("last_taken_" + med.id).apply();
            remove(med);
            onChange.run();
        });

        return convertView;
    }

    private void sendStatus(String medName, String status) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String key = medName.replaceAll("[.#$\\[\\]/]", "_");
        FirebaseDatabase.getInstance().getReference("patients")
                .child(user.getUid()).child("status").child(key)
                .setValue(medName + ": " + status);
    }
}