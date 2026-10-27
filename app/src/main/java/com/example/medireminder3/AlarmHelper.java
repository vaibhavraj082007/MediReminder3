package com.example.medireminder3;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

public class AlarmHelper {

    // rings the same alarm again after a few minutes (no daily repeat)
    public static void snooze(Context ctx, String name, int dose, int code, int minutes) {
        Intent intent = new Intent(ctx, ReminderReceiver.class);
        intent.putExtra("name", name);
        intent.putExtra("dose", dose);
        intent.putExtra("id", code);
        intent.putExtra("hour", -1);   // -1 = do not repeat

        PendingIntent pi = PendingIntent.getBroadcast(ctx, 100000 + code, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        setExact(ctx, pi, System.currentTimeMillis() + minutes * 60000L);
    }

    // one alarm for every chosen time
    public static void setReminders(Context ctx, Medicine m, int[] hours, int[] minutes) {
        for (int i = 0; i < hours.length; i++) {
            scheduleOne(ctx, m.name, m.doseQty, m.id * 10 + i, hours[i], minutes[i]);
        }
    }

    // schedules the NEXT time this clock time happens (today or tomorrow)
    public static void scheduleOne(Context ctx, String name, int dose,
                                   int requestCode, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.before(Calendar.getInstance())) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(ctx, ReminderReceiver.class);
        intent.putExtra("name", name);
        intent.putExtra("dose", dose);
        intent.putExtra("id", requestCode);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);

        PendingIntent pi = PendingIntent.getBroadcast(ctx, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        setExact(ctx, pi, cal.getTimeInMillis());
    }

    // uses the alarm-clock method (same as the Clock app), always fires on time
    private static void setExact(Context ctx, PendingIntent pi, long timeMillis) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent show = new Intent(ctx, LoginActivity.class);
        PendingIntent showPi = PendingIntent.getActivity(ctx, 0, show,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.setAlarmClock(new AlarmManager.AlarmClockInfo(timeMillis, showPi), pi);
    }

    public static void cancelReminder(Context ctx, int medicineId) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        for (int i = 0; i < 10; i++) {
            Intent intent = new Intent(ctx, ReminderReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(ctx, medicineId * 10 + i, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            am.cancel(pi);
        }
    }
}