package com.sankalp.womensafe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class PowerButtonService extends Service {

    private static final String TAG = "PowerButtonService";
    private static final String CHANNEL_ID = "PowerButtonServiceChannel";
    private static final int PRESS_INTERVAL = 3000; // 3 seconds
    private static final int PRESS_COUNT_LIMIT = 5;

    private int pressCount = 0;
    private long lastPressTime = 0;

    private final BroadcastReceiver powerButtonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                long currentTime = System.currentTimeMillis();

                if (currentTime - lastPressTime <= PRESS_INTERVAL) {
                    pressCount++;
                } else {
                    pressCount = 1;
                }
                lastPressTime = currentTime;

                if (pressCount == PRESS_COUNT_LIMIT) {
                    Log.d(TAG, "SOS pattern detected! Triggering SOS.");
                    pressCount = 0; // Reset count
                    
                    Intent sosIntent = new Intent("com.sankalp.womensafe.TRIGGER_SOS");
                    sosIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(sosIntent);
                }
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Log.d(TAG, "PowerButtonService created.");
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(powerButtonReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "PowerButtonService started.");

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Digital Guardian Active")
                .setContentText("Hardware button SOS trigger is enabled.")
                .setSmallIcon(R.mipmap.ic_launcher) // CORRECTED: Use a valid icon
                .build();

        startForeground(2, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "PowerButtonService destroyed.");
        unregisterReceiver(powerButtonReceiver);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Power Button SOS Service",
                    NotificationManager.IMPORTANCE_LOW // Use low importance for background services
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
