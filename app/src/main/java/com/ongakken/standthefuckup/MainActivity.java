package com.ongakken.standthefuckup;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import com.google.android.material.button.MaterialButton;

import java.util.Timer;

public class MainActivity extends AppCompatActivity {

    private TextView timerTextView;
    private MaterialButton startTimerButton;
    private MaterialButton markAsDoneButton;
    private MaterialButton postponeButton;
    private TimerSvc timerService;
    private boolean serviceBound;
    private TimerBroadcastReceiver timerBroadcastReceiver;

    private BroadcastReceiver timerUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long millisUntilFinished = intent.getLongExtra("millisUntilFinished", 0);
            updateTimeTextView(millisUntilFinished);
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerSvc.TimerBinder binder = (TimerSvc.TimerBinder) service;
            timerService = binder.getService();
            long timeLeftInMillis = loadTimeLeftInMillis();
            updateTimeTextView(timeLeftInMillis);
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void updateTimeTextView(long millisUntilFinished) {
        int min = (int) (millisUntilFinished / 1000) / 60;
        int sex = (int) (millisUntilFinished / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", min, sex);
        timerTextView.setText(timeFormatted);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.FOREGROUND_SERVICE,
        }, 1);

        timerTextView = findViewById(R.id.timerTextView);
        startTimerButton = findViewById(R.id.startTimerButton);
        markAsDoneButton = findViewById(R.id.markAsDoneButton);
        postponeButton = findViewById(R.id.postponeButton);

        startTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("onclick", "Clicked START TIMER!");
                if (serviceBound) {
                    Log.i("onclick", "We're serviceBound!");
                    timerService.startTimer();
                    startTimerButton.setText("Pause");
                    markAsDoneButton.setVisibility(View.INVISIBLE);
                    postponeButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        markAsDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceBound) {
                    timerService.resetTimer();
                    timerService.startTimer();
                    startTimerButton.setText("Pause");
                    markAsDoneButton.setVisibility(View.INVISIBLE);
                    postponeButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        postponeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceBound) {
                    timerService.postponeTimer(5 * 60 * 1000);
                    timerService.startTimer();
                    startTimerButton.setText("Pause");
                    markAsDoneButton.setVisibility(View.INVISIBLE);
                    postponeButton.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TimerSvc.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        timerBroadcastReceiver = new TimerBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("com.ongakken.standthefuckup.TIMER_UPDATE");
        registerReceiver(timerBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
        unregisterReceiver(timerBroadcastReceiver);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "timer_svc_channel",
                    "Timer Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );

            notificationChannel.setDescription("Channel for Timer notifications");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(notificationChannel);
            }
        }
    }
    public class TimerBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long millisUntilFinished = intent.getLongExtra("millisUntilFinished", 0);
            updateTimeTextView(millisUntilFinished);
        }
    }

    private long loadTimeLeftInMillis() {
        SharedPreferences sharedPreferences = getSharedPreferences("timer_prefs", MODE_PRIVATE);
        return sharedPreferences.getLong("time_left_in_millis", 25 * 60 * 1000);
    }
}
