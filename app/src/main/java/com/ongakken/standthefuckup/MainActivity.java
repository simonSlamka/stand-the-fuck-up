package com.ongakken.standthefuckup;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private TextView timerTextView;
    private MaterialButton startTimerButton;
    private MaterialButton markAsDoneButton;
    private MaterialButton postponeButton;

    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long timeLeftInMillis = 25 * 60 * 1000;

    private TimerSvc timerService;
    private boolean serviceBound;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerSvc.TimerBinder binder = (TimerSvc.TimerBinder) service;
            timerService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.POST_NOTIFICATIONS
        }, 1);

        timerTextView = findViewById(R.id.timerTextView);
        startTimerButton = findViewById(R.id.startTimerButton);
        markAsDoneButton = findViewById(R.id.markAsDoneButton);
        postponeButton = findViewById(R.id.postponeButton);

        startTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        markAsDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Your code to handle "Mark as Done" action
                resetTimer();
                startTimer();
            }
        });

        postponeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Your code to handle "Postpone" action
                timeLeftInMillis += 5 * 60 * 1000;
                startTimer();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TimerSvc.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                startTimerButton.setText("Start");
                markAsDoneButton.setVisibility(View.VISIBLE);
                postponeButton.setVisibility(View.VISIBLE);

                if (serviceBound) {
                    timerService.stopForegroundService();
                }
            }
        }.start();

        timerRunning = true;
        startTimerButton.setText("Pause");
        markAsDoneButton.setVisibility(View.INVISIBLE);

        postponeButton.setVisibility(View.INVISIBLE);

        if (serviceBound) {
            timerService.startForegroundService();
        }
    }

    private void pauseTimer() {
        countDownTimer.cancel();
        timerRunning = false;
        startTimerButton.setText("Start");
        markAsDoneButton.setVisibility(View.VISIBLE);
        postponeButton.setVisibility(View.VISIBLE);

        if (serviceBound) {
            timerService.stopForegroundService();
        }
    }

    private void resetTimer() {
        timeLeftInMillis = 25 * 60 * 1000;
        updateCountDownText();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        timerTextView.setText(timeFormatted);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "TimerChannelID",
                    "Timer Channel",
                    NotificationManager.IMPORTANCE_LOW
            );

            notificationChannel.setDescription("Channel for Timer notifications");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(notificationChannel);
            }
        }
    }
}
