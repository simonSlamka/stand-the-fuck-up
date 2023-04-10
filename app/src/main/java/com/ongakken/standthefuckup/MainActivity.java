package com.ongakken.standthefuckup;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView timerTextView;
    private Button startTimerButton;
    private Button markAsDoneButton;
    private Button postponeButton;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 25 * 60 * 1000;
    private List<String> userActionsLog = new ArrayList<>();

    private final String CHANNEL_ID = "work_break_channel";
    private final int NOTIFICATION_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerTextView = findViewById(R.id.timerTextView);
        startTimerButton = findViewById(R.id.startTimerButton);
        markAsDoneButton = findViewById(R.id.markAsDoneButton);
        postponeButton = findViewById(R.id.postponeButton);

        createNotificationChannel();

        startTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer();
            }
        });

        markAsDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logAction("Mark as Done");
                startTimer();
            }
        });

        postponeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logAction("Postpone by 5min");
                timeLeftInMillis += 5 * 60 * 1000;
                startTimer();
            }
        });

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_NOTIFICATION_POLICY}, 1);
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                sendWorkoutNotification();
                logAction("Timer Finished");
                timeLeftInMillis = 25 * 60 * 1000;
                updateCountDownText();
            }
        }.start();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        timerTextView.setText(timeFormatted);
    }

    private void sendWorkoutNotification() {
        String workout = getRandomWorkout();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Time for a work break")
                .setContentText("Suggested workout: " + workout)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private String getRandomWorkout() {
        // Add your workout suggestions here
        String[] workouts = {
                "Jumping Jacks",
                "Push-ups",
                "Plank",
                "Lunges",
                "Squats"
        };

        Random random = new Random();
        return workouts[random.nextInt(workouts.length)];
    }

    private void logAction(String action) {
        long currentTimeMillis = System.currentTimeMillis();
        String timestamp = String.format("%1$tF %1$tT", currentTimeMillis);
        userActionsLog.add(timestamp + ", " + action);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Work Break";
            String description = "Work break notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted. You can now export logs to CSV when required.
            } else {
                // Permission denied. You won't be able to export logs to CSV.
            }
        }
    }
}
