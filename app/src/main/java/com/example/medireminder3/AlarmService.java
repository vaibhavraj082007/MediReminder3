package com.example.medireminder3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {

    static final String CHANNEL_ID = "medi_alarm_30s";
    static final long RING_MILLIS = 30000;   // 30 seconds

    MediaPlayer player;
    Vibrator vibrator;
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable stopTask = this::stopSelf;

    String name;
    int dose, code;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (intent != null && "SNOOZE".equals(intent.getAction())) {
            AlarmHelper.snooze(this, intent.getStringExtra("name"),
                    intent.getIntExtra("dose", 1), intent.getIntExtra("id", 1), 10);
            android.widget.Toast.makeText(this, "Snoozed for 10 minutes",
                    android.widget.Toast.LENGTH_SHORT).show();
            name = null;   // so no extra notification is left behind
            stopSelf();
            return START_NOT_STICKY;
        }
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        name = intent.getStringExtra("name");
        dose = intent.getIntExtra("dose", 1);
        code = intent.getIntExtra("id", 1);

        NotificationManager nm =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Medicine alarms", NotificationManager.IMPORTANCE_HIGH);
            ch.setSound(null, null);       // the service plays the sound itself
            ch.enableVibration(false);
            nm.createNotificationChannel(ch);
        }

        Notification n = buildNotification(true);
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(code, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(code, n);
        }

        startSound();

        handler.removeCallbacks(stopTask);
        handler.postDelayed(stopTask, RING_MILLIS);

        return START_NOT_STICKY;
    }

    private Notification buildNotification(boolean ringing) {
        Intent open = new Intent(this, LoginActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(this, code, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Time for your medicine")
                .setContentText("Take " + dose + " tablet(s) of " + name)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(openPi)
                .setAutoCancel(true);

        if (ringing) {
            Intent stop = new Intent(this, AlarmService.class);
            stop.setAction("STOP");
            PendingIntent stopPi = PendingIntent.getService(this, 0, stop,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            b.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop alarm", stopPi);
            Intent snooze = new Intent(this, AlarmService.class);
            snooze.setAction("SNOOZE");
            snooze.putExtra("name", name);
            snooze.putExtra("dose", dose);
            snooze.putExtra("id", code);
            PendingIntent snoozePi = PendingIntent.getService(this, 1, snooze,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            b.addAction(android.R.drawable.ic_media_pause, "Snooze 10 min", snoozePi);
        }
        return b.build();
    }

    @SuppressWarnings("deprecation")
    private void startSound() {
        stopSound();

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        try {
            player = new MediaPlayer();
            player.setDataSource(this, uri);
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            player.setLooping(true);
            player.prepare();
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            long[] pattern = {0, 800, 400};
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(pattern, 0);
            }
        }
    }

    private void stopSound() {
        android.media.AudioManager audio =
                (android.media.AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.setStreamVolume(android.media.AudioManager.STREAM_ALARM,
                audio.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM), 0);
        if (player != null) {
            try {
                player.stop();
            } catch (Exception ignored) { }
            player.release();
            player = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(stopTask);
        stopSound();
        stopForeground(STOP_FOREGROUND_REMOVE);

        // leave a normal (silent) reminder in the notification bar
        if (name != null) {
            NotificationManager nm =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(code, buildNotification(false));
        }
        super.onDestroy();
    }
}