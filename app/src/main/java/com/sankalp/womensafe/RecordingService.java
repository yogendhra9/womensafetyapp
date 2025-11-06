package com.sankalp.womensafe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class RecordingService extends Service {

    private static final String TAG = "RecordingService";
    private static final String CHANNEL_ID = "RecordingServiceChannel";
    private MediaRecorder mediaRecorder;
    private String outputFile;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Recording service starting...");

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Digital Guardian")
                .setContentText("SOS Activated: Recording audio.")
                .setSmallIcon(R.mipmap.ic_launcher) // Use a valid icon
                .build();

        startForeground(1, notification);

        startRecording();

        return START_STICKY;
    }

    private void startRecording() {
        // Use a more modern and compatible file format and extension
        outputFile = getExternalFilesDir(null).getAbsolutePath() + "/SOS_Recording_" + System.currentTimeMillis() + ".mp4";

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // Use a more modern and compatible output format and encoder
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(outputFile);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            Log.d(TAG, "Recording started successfully. Output file: " + outputFile);
            // Show toast in a separate thread to avoid potential UI issues
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                Toast.makeText(this, "Recording Started.", Toast.LENGTH_SHORT).show();
            });
        } catch (IOException | IllegalStateException e) {
            Log.e(TAG, "MediaRecorder prepare() or start() failed", e);
            // Show toast in a separate thread to avoid potential UI issues
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                Toast.makeText(this, "Error: Could not start audio recording.", Toast.LENGTH_LONG).show();
            });
            // Stop the service if recording fails to start
            stopSelf(); 
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                Log.d(TAG, "Recording stopped successfully.");
            } catch (RuntimeException stopException) {
                // This can happen if the service is stopped before the recording is properly started
                Log.w(TAG, "RuntimeException while stopping recorder: " + stopException.getMessage());
            } finally {
                mediaRecorder.release();
                mediaRecorder = null;
                Log.d(TAG, "MediaRecorder released.");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Recording service stopped.");
        stopRecording();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recording Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
