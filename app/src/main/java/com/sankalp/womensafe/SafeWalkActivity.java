package com.sankalp.womensafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class SafeWalkActivity extends AppCompatActivity {

    private EditText timerEditText;
    private Button startTimerButton;
    private TextView countdownText;
    private Button imSafeButton;
    private LinearLayout setupView;
    private LinearLayout countdownView;

    private CountDownTimer countDownTimer;
    private boolean timerRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_walk);

        timerEditText = findViewById(R.id.edit_text_timer);
        startTimerButton = findViewById(R.id.button_start_timer);
        countdownText = findViewById(R.id.text_countdown);
        imSafeButton = findViewById(R.id.button_im_safe);
        setupView = findViewById(R.id.setup_view);
        countdownView = findViewById(R.id.countdown_view);

        startTimerButton.setOnClickListener(v -> {
            if (!timerRunning) {
                startTimer();
            }
        });

        imSafeButton.setOnClickListener(v -> stopTimer());
    }

    private void startTimer() {
        String timerInput = timerEditText.getText().toString();
        if (timerInput.isEmpty()) {
            Toast.makeText(this, "Please enter a time", Toast.LENGTH_SHORT).show();
            return;
        }

        long timeInMillis = Long.parseLong(timerInput) * 60000;

        countDownTimer = new CountDownTimer(timeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateCountdownText(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                triggerSos();
            }
        }.start();

        timerRunning = true;
        updateUiForTimerRunning();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerRunning = false;
        
        Toast.makeText(this, "Timer cancelled. You are safe!", Toast.LENGTH_LONG).show();

        // Disable the button to prevent multiple clicks and show the timer has stopped
        imSafeButton.setEnabled(false);
        countdownView.setAlpha(0.5f); // Dim the view to show it's inactive

        // Wait for 2 seconds before closing the activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            finish();
        }, 2000);
    }

    private void updateCountdownText(long millisUntilFinished) {
        int minutes = (int) (millisUntilFinished / 1000) / 60;
        int seconds = (int) (millisUntilFinished / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        countdownText.setText(timeLeftFormatted);
    }

    private void triggerSos() {
        Toast.makeText(this, "TIMER FINISHED! Triggering SOS...", Toast.LENGTH_LONG).show();
        Intent intent = new Intent("com.sankalp.womensafe.TRIGGER_SOS");
        sendBroadcast(intent);
        finish(); // Close the activity
    }

    private void updateUiForTimerRunning() {
        setupView.setVisibility(View.GONE);
        countdownView.setVisibility(View.VISIBLE);
    }

    // This method is no longer needed as we don't return to the setup view
    private void updateUiForTimerStopped() { 
        // No longer reverting the UI, the activity will close
    }
}
