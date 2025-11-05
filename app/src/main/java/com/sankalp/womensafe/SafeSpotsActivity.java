package com.sankalp.womensafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

public class SafeSpotsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "SafeSpotsActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_spots);

        // TEMPORARY: Commented out to prevent crash. 
        // TODO: Uncomment this and add a valid API key to make the map work.
        // Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        // placesClient = Places.createClient(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Map is ready.");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocationAndFindPoliceStations();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocationAndFindPoliceStations() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(this, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location currentLocation = task.getResult();
                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f));
                // findNearbyPoliceStations(currentLatLng); // Also commented out as it depends on PlacesClient
            } else {
                Log.e(TAG, "Current location is null.", task.getException());
            }
        });
    }

    private void findNearbyPoliceStations(LatLng location) {
        // This method is temporarily disabled until the API key is provided.
        if (placesClient == null) {
            Toast.makeText(this, "Maps API key is missing.", Toast.LENGTH_LONG).show();
            return;
        }

        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.TYPES);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placeFields).build();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Task<FindCurrentPlaceResponse> placeResult = placesClient.findCurrentPlace(request);
        placeResult.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                FindCurrentPlaceResponse response = task.getResult();
                int count = 0;
                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                    Place place = placeLikelihood.getPlace();
                    if (place.getTypes() != null && place.getTypes().contains(Place.Type.POLICE)) {
                        mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName()));
                        count++;
                    }
                }
                if (count == 0) {
                    Toast.makeText(this, "No police stations found nearby.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Exception: %s", task.getException());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocationAndFindPoliceStations();
            } else {
                Toast.makeText(this, "Location permission is required to find safe spots.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
