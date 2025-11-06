package com.sankalp.womensafe;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class RecordingsActivity extends AppCompatActivity {

    private static final String TAG = "RecordingsActivity";

    private ListView recordingsListView;
    private TextView emptyView;
    private ArrayList<File> recordingFiles;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordings);

        recordingsListView = findViewById(R.id.list_recordings);
        emptyView = findViewById(R.id.empty_recordings_view);

        loadRecordings();

        recordingsListView.setOnItemClickListener((parent, view, position, id) -> {
            playRecording(recordingFiles.get(position));
        });
    }

    private void loadRecordings() {
        File recordingsDir = getExternalFilesDir(null);
        recordingFiles = new ArrayList<>();
        ArrayList<String> recordingNames = new ArrayList<>();

        if (recordingsDir != null && recordingsDir.exists()) {
            File[] files = recordingsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".3gp") || file.getName().startsWith("SOS_Recording_")) {
                        recordingFiles.add(file);
                        recordingNames.add(file.getName());
                    }
                }
            }
        }

        // Sort recordings by date, newest first
        Collections.sort(recordingFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        Collections.sort(recordingNames, Collections.reverseOrder());

        if (recordingFiles.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recordingsListView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recordingsListView.setVisibility(View.VISIBLE);
            
            // Create adapter with more detailed information
            ArrayList<String> detailedRecordingInfo = new ArrayList<>();
            for (File file : recordingFiles) {
                String fileName = file.getName();
                long fileSize = file.length(); // in bytes
                long timestamp = file.lastModified(); // in milliseconds
                
                // Format file size to be more readable
                String formattedSize = formatFileSize(fileSize);
                String formattedDate = android.text.format.DateFormat.format("MMM dd, yyyy hh:mm a", new java.util.Date(timestamp)).toString();
                
                String fileInfo = fileName + "\n" + formattedSize + " | " + formattedDate;
                detailedRecordingInfo.add(fileInfo);
            }
            
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_2, 
                android.R.id.text1, detailedRecordingInfo);
            recordingsListView.setAdapter(adapter);
        }
    }

    private MediaPlayer mediaPlayer;

    private void playRecording(File file) {
        // Stop any currently playing recording
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(this, "Playing: " + file.getName(), Toast.LENGTH_SHORT).show();

            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayer = null;
                Toast.makeText(this, "Playback finished.", Toast.LENGTH_SHORT).show();
            });

        } catch (IOException e) {
            Log.e(TAG, "Failed to play recording", e);
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            Toast.makeText(this, "Could not play recording.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release media player when activity is destroyed
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) return sizeInBytes + " B";
        if (sizeInBytes < 1024 * 1024) return String.format("%.1f KB", sizeInBytes / 1024.0);
        if (sizeInBytes < 1024 * 1024 * 1024) return String.format("%.1f MB", sizeInBytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", sizeInBytes / (1024.0 * 1024.0 * 1024.0));
    }
}
