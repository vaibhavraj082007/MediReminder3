package com.example.medireminder3;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class AlertHelper {

    // red = warnings (low stock, problems)
    public static void redToast(Context ctx, String message) {
        show(ctx, "⚠  " + message, "#D32F2F");
    }

    // green = success (account accessed, account created)
    public static void greenToast(Context ctx, String message) {
        show(ctx, "✔  " + message, "#2E7D32");
    }

    private static void show(Context ctx, String message, String colorHex) {
        Context app = ctx.getApplicationContext();
        float d = app.getResources().getDisplayMetrics().density;

        TextView tv = new TextView(app);
        tv.setText(message);
        tv.setTextColor(Color.WHITE);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(14);
        int p = (int) (14 * d);
        tv.setPadding(p, p, p, p);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(colorHex));
        bg.setCornerRadius(12 * d);
        tv.setBackground(bg);

        Toast t = new Toast(app);
        t.setView(tv);
        t.setDuration(Toast.LENGTH_LONG);
        t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, (int) (80 * d));
        t.show();
    }
}