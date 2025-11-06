package com.sankalp.womensafe;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "TrustedContacts";
    private static final String CONTACTS_KEY = "contacts";

    TextView sosButton;
    Button stopSosButton;
    View panicOverlay;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private BroadcastReceiver sosBroadcastReceiver;
    private static boolean isSosActive = false;
    private float originalBrightness = -1f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sosButton = findViewById(R.id.button_sos);
        stopSosButton = findViewById(R.id.button_stop_sos);
        panicOverlay = findViewById(R.id.panic_overlay);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        sosButton.setOnClickListener(v -> {
            if (isSosActive) {
                stopSosLogic();
            } else {
                requestPermissionsAndStartSos();
            }
        });
        stopSosButton.setOnClickListener(v -> stopSosLogic());

        setupSosBroadcastReceiver();
        updateUiBasedOnSosState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUiBasedOnSosState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sosBroadcastReceiver != null) {
            unregisterReceiver(sosBroadcastReceiver);
        }
    }

    private void updateUiBasedOnSosState() {
        if (isSosActive) {
            updateUiForSosActive();
        } else {
            updateUiForSosInactive();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_contacts) {
            startActivity(new Intent(this, ContactsActivity.class));
            return true;
        } else if (itemId == R.id.action_safe_walk) {
            startActivity(new Intent(this, SafeWalkActivity.class));
            return true;
        } else if (itemId == R.id.action_fake_call) {
            startActivity(new Intent(this, FakeCallActivity.class));
            return true;
        } else if (itemId == R.id.action_safe_spots) {
            startActivity(new Intent(this, SafeSpotsActivity.class));
            return true;
        } else if (itemId == R.id.action_recordings) {
            startActivity(new Intent(this, RecordingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSosBroadcastReceiver() {
        sosBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isSosActive) {
                    Log.d(TAG, "SOS broadcast received.");
                    requestPermissionsAndStartSos();
                }
            }
        };
        registerReceiver(sosBroadcastReceiver, new IntentFilter("com.sankalp.womensafe.TRIGGER_SOS"), RECEIVER_NOT_EXPORTED);
    }

    private void requestPermissionsAndStartSos() {
        if (isSosActive) {
            Log.w(TAG, "SOS is already active. Ignoring request.");
            return;
        }

        List<String> permissionsToRequest = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO);
        }
        // Add POST_NOTIFICATIONS for Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSIONS_REQUEST_CODE);
        } else {
            startSosLogic();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                startSosLogic();
            } else {
                Toast.makeText(this, "All permissions are required for SOS to function.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startSosLogic() {
        if (isSosActive) return;
        Log.d(TAG, "SOS Logic Started!");
        isSosActive = true;
        updateUiForSosActive();

        if (originalBrightness < 0) {
            try {
                originalBrightness = getWindow().getAttributes().screenBrightness;
            } catch (Exception e) {
                originalBrightness = 0.5f;
            }
        }
        setScreenBrightness(0.0f);

        Intent serviceIntent = new Intent(this, RecordingService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
             locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    sendSmsToTrustedContacts(location);
                    locationManager.removeUpdates(this);
                }
            };
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } catch (SecurityException e) {
                Log.e(TAG, "Location permission error", e);
            }
        }
    }

    private void stopSosLogic() {
        if (!isSosActive) return;
        Log.d(TAG, "Stopping SOS logic.");
        isSosActive = false;
        updateUiForSosInactive();
        restoreScreenBrightness();
        stopService(new Intent(this, RecordingService.class));
        Toast.makeText(this, "SOS Deactivated.", Toast.LENGTH_SHORT).show();
    }

    private void sendSmsToTrustedContacts(Location location) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Set<String> contacts = sharedPreferences.getStringSet(CONTACTS_KEY, new HashSet<>());
            if (contacts.isEmpty()) {
                Toast.makeText(this, "No trusted contacts. Please add them.", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                String message = "EMERGENCY! My location is: http://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                SmsManager smsManager = SmsManager.getDefault();
                for (String contact : contacts) {
                    smsManager.sendTextMessage(contact, null, message, null, null);
                }
                Toast.makeText(this, "SOS SMS sent to all trusted contacts!", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(TAG, "SMS failed to send.", e);
            }
        }
    }

    private void setScreenBrightness(float brightness) {
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = brightness;
        getWindow().setAttributes(layout);
    }

    private void restoreScreenBrightness() {
        if (originalBrightness >= 0) {
            setScreenBrightness(originalBrightness);
        }
    }

    private void updateUiForSosActive() {
        sosButton.setVisibility(View.GONE);
        stopSosButton.setVisibility(View.VISIBLE);
        panicOverlay.setVisibility(View.VISIBLE);
    }

    private void updateUiForSosInactive() {
        sosButton.setVisibility(View.VISIBLE);
        stopSosButton.setVisibility(View.GONE);
        panicOverlay.setVisibility(View.GONE);
    }
}
