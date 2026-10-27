package com.example.medireminder3;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    DoseLogStore store;
    TextView tvTaken, tvMissed, tvScore, tvDayTitle, tvDayCounts;
    LinearLayout llDayList;
    float d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // we draw our own header, so hide the default top bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        d = getResources().getDisplayMetrics().density;
        store = new DoseLogStore(this);

        tvTaken = findViewById(R.id.tvTaken);
        tvMissed = findViewById(R.id.tvMissed);
        tvScore = findViewById(R.id.tvScore);
        tvDayTitle = findViewById(R.id.tvDayTitle);
        tvDayCounts = findViewById(R.id.tvDayCounts);
        llDayList = findViewById(R.id.llDayList);
        CalendarView cv = findViewById(R.id.calendarView);

        // keep the header clear of the status bar
        View header = findViewById(R.id.header);
        ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(v.getPaddingLeft(), top + (int) (12 * d),
                    v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        roundBg(findViewById(R.id.cardTaken), "#C8E6C9", 18);
        roundBg(findViewById(R.id.cardMissed), "#FFCDD2", 18);
        roundBg(findViewById(R.id.cardScore), "#DCEDC8", 18);

        int taken = store.countAll("taken");
        int missed = store.countAll("missed");
        tvTaken.setText(String.valueOf(taken));
        tvMissed.setText(String.valueOf(missed));
        tvScore.setText(taken + missed == 0 ? "--" : ((taken * 100) / (taken + missed)) + "%");

        showDay(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));

        cv.setOnDateChangeListener((view, y, m, day) ->
                showDay(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, day)));
    }

    private int dp(int v) {
        return (int) (v * d);
    }

    private void roundBg(View v, String color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.parseColor(color));
        g.setCornerRadius(radiusDp * d);
        v.setBackground(g);
    }

    private void showDay(String day) {
        int taken = store.countDay(day, "taken");
        int missed = store.countDay(day, "missed");

        String pretty = day;
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(day);
            pretty = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(date);
        } catch (Exception ignored) { }

        tvDayTitle.setText(pretty);
        tvDayCounts.setText(taken + " taken  •  " + missed + " missed");

        llDayList.removeAllViews();
        ArrayList<String> rows = store.forDay(day);

        if (rows.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No records for this day.");
            empty.setTextColor(Color.parseColor("#6B6470"));
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(16), dp(28), dp(16), dp(28));
            empty.setBackgroundResource(R.drawable.bg_card);
            llDayList.addView(empty, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            return;
        }

        for (String row : rows) addRow(row);
    }

    // row format from DoseLogStore: "hh:mm a   name  -  ✔ Taken"
    private void addRow(String row) {
        boolean taken = row.endsWith("Taken");
        String time = "";
        String name = row;
        int i = row.indexOf("   ");
        int j = row.lastIndexOf("  -  ");
        if (i > 0 && j > i) {
            time = row.substring(0, i);
            name = row.substring(i + 3, j).trim();
        }

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));

        TextView tvTime = new TextView(this);
        tvTime.setText(time);
        tvTime.setTextColor(Color.parseColor("#2E7D32"));
        tvTime.setTextSize(14);
        tvTime.setTypeface(null, Typeface.BOLD);
        tvTime.setMinWidth(dp(78));

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(Color.parseColor("#1B3A1F"));
        tvName.setTextSize(16);

        TextView chip = new TextView(this);
        chip.setText(taken ? "Taken" : "Missed");
        chip.setTextColor(Color.WHITE);
        chip.setTextSize(12);
        chip.setTypeface(null, Typeface.BOLD);
        chip.setPadding(dp(12), dp(5), dp(12), dp(5));
        roundBg(chip, taken ? "#2E7D32" : "#C62828", 20);

        card.addView(tvTime);
        card.addView(tvName, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        card.addView(chip);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(8));
        llDayList.addView(card, lp);
    }
}