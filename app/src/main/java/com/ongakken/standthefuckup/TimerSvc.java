package com.ongakken.standthefuckup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class TimerSvc extends Service {
    public static final String CHANNEL_ID = "timer_svc_channel";

    private final IBinder binder = new TimerBinder();

    public class TimerBinder extends Binder {
        TimerSvc getService() {
            return TimerSvc.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void startForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, "TimerChannelID")
                .setContentTitle("Work Break Timer")
                .setContentText("Timer is running")
                //.setSmallIcon(R.drawable.ic_timer)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    public void stopForegroundService() {
        stopForeground(true);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundService();

        return START_STICKY;
    }
}