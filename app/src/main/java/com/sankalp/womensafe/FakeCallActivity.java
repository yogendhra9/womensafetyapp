package com.sankalp.womensafe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FakeCallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call);

        Button acceptButton = findViewById(R.id.button_accept_call);
        Button declineButton = findViewById(R.id.button_decline_call);

        View.OnClickListener finishActivityListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Simply close the fake call screen
            }
        };

        acceptButton.setOnClickListener(finishActivityListener);
        declineButton.setOnClickListener(finishActivityListener);
    }
}
