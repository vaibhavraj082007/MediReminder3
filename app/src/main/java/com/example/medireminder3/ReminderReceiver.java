package com.example.medireminder3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String name = intent.getStringExtra("name");
        int dose = intent.getIntExtra("dose", 1);
        int code = intent.getIntExtra("id", 0);
        int hour = intent.getIntExtra("hour", -1);
        int minute = intent.getIntExtra("minute", 0);

        // start the 30 second alarm sound
        Intent svc = new Intent(ctx, AlarmService.class);
        svc.putExtra("name", name);
        svc.putExtra("dose", dose);
        svc.putExtra("id", code);
        ContextCompat.startForegroundService(ctx, svc);

        // set the same alarm again for tomorrow (hour -1 means no repeat)
        if (hour >= 0) {
            AlarmHelper.scheduleOne(ctx, name, dose, code, hour, minute);
        }
    }
}