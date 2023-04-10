package com.ongakken.standthefuckup;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class TimerSvc extends Service {

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
}