package com.ongakken.standthefuckup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;

public class TimerSvc extends Service {
    public static final String CHANNEL_ID = "timer_svc_channel";
    public static final String TIMER_UPDATED_ACTION = "com.ongakken.standthefuckup.TIMER_UPDATED";
    private final IBinder binder = new TimerBinder();
    private PowerManager.WakeLock wakeLock;

    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StandTheFuckUp::TimerWakeLock");
            wakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

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
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent dummyDelIntent = new Intent();
        PendingIntent delPendingIntent = PendingIntent.getBroadcast(this, 0, dummyDelIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, "timer_svc_channel")
                .setContentTitle("Stand the FUCK Up!!")
                .setContentText("Timer running")
                //.setSmallIcon(R.drawable.ic_timer)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        startForeground(1, notification);
    }

    public void stopForegroundService() {
        stopForeground(false);
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
        acquireWakeLock();

        return START_STICKY;
    }

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 25 * 60 * 1000;

    public void startTimer() {
        startForegroundService();
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                Intent intent = new Intent("com.ongakken.standthefuckup.TIMER_UPDATE");
                intent.putExtra("millisUntilFinished", millisUntilFinished);
                sendBroadcast(intent);
            }

            @Override
            public void onFinish() {
                // beep boop lock
            }
        }.start();
    }

    public void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void resetTimer() {
        timeLeftInMillis = 25 * 60 * 1000;
    }

    public void postponeTimer(long millisToPostpone) {
        timeLeftInMillis += millisToPostpone;
    }

    private void saveTimeLeftInMillis(long millis) {
        SharedPreferences sharedPrefs = getSharedPreferences("timer_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putLong("time_left_in_millis", millis);
        editor.apply();
    }

    public interface TimerUpdateListener {
        void onTimeUpdated(long millisUntilFinished);
    }

    private TimerUpdateListener timerUpdateListener;

    public void setTimerUpdateListener(TimerUpdateListener listener) {
        timerUpdateListener = listener;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
    }
}