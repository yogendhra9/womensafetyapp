//package com.sankalp.womensafe;
//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.tasks.Task;
//import com.google.android.libraries.places.api.Places;
//import com.google.android.libraries.places.api.model.AutocompletePrediction;
//import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
//import com.google.android.libraries.places.api.model.OpeningHours;
//import com.google.android.libraries.places.api.model.Place;
//import com.google.android.libraries.places.api.net.FetchPlaceRequest;
//import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
//import com.google.android.libraries.places.api.net.PlacesClient;
//
//import java.util.Arrays;
//import java.util.List;
//
//public class SafeSpotsActivity extends AppCompatActivity implements OnMapReadyCallback {
//
//    private static final String TAG = "SafeSpotsActivity";
//    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
//
//    private GoogleMap mMap;
//    private FusedLocationProviderClient fusedLocationProviderClient;
//    private PlacesClient placesClient;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_safe_spots);
//
//        if (getString(R.string.google_maps_key).equals("YOUR_API_KEY_HERE")) {
//            Toast.makeText(this, "ERROR: Add your Google Maps API key in strings.xml", Toast.LENGTH_LONG).show();
//            Log.e(TAG, "API key is not set. Please set it in strings.xml");
//            finish();
//            return;
//        }
//
//        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
//        placesClient = Places.createClient(this);
//
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        if (mapFragment != null) {
//            mapFragment.getMapAsync(this);
//        }
//    }
//
//    @Override
//    public void onMapReady(@NonNull GoogleMap googleMap) {
//        mMap = googleMap;
//        Log.d(TAG, "Map is ready.");
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            getDeviceLocationAndFindOpenPlaces();
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    LOCATION_PERMISSION_REQUEST_CODE);
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    private void getDeviceLocationAndFindOpenPlaces() {
//        mMap.setMyLocationEnabled(true);
//        mMap.getUiSettings().setMyLocationButtonEnabled(true);
//
//        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
//        locationResult.addOnCompleteListener(this, task -> {
//            if (task.isSuccessful() && task.getResult() != null) {
//                Location currentLocation = task.getResult();
//                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
//                findNearbyOpenPlaces(currentLatLng);
//            } else {
//                Log.e(TAG, "Current location is null.", task.getException());
//                Toast.makeText(this,
//                        "Could not get current location. Make sure location is enabled.",
//                        Toast.LENGTH_LONG).show();
//            }
//        });
//    }
//
//    private void findNearbyOpenPlaces(LatLng location) {
//        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
//
//        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
//                .setQuery("hospital police station pharmacy restaurant cafe store supermarket")
//                .setOrigin(location)
//                .setSessionToken(token)
//                .build();
//
//        placesClient.findAutocompletePredictions(request)
//                .addOnSuccessListener((response) -> {
//                    if (response.getAutocompletePredictions().isEmpty()) {
//                        Toast.makeText(this, "No public places found nearby.", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
//                        String placeId = prediction.getPlaceId();
//
//                        List<Place.Field> placeFields = Arrays.asList(
//                                Place.Field.ID,
//                                Place.Field.NAME,
//                                Place.Field.LAT_LNG,
//                                Place.Field.OPENING_HOURS
//                        );
//
//                        FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
//
//                        placesClient.fetchPlace(fetchPlaceRequest)
//                                .addOnSuccessListener((fetchPlaceResponse) -> {
//                                    Place place = fetchPlaceResponse.getPlace();
//
//                                    if (place != null && place.getOpeningHours() != null) {
//                                        // DEFINITIVE FIX: Use isOpen with the current time.
//                                        Boolean isOpen = place.getOpeningHours().isOpen(System.currentTimeMillis());
//                                        if (isOpen != null && isOpen) {
//                                            Log.i(TAG, "Open spot found: " + place.getName());
//                                            mMap.addMarker(new MarkerOptions()
//                                                    .position(place.getLatLng())
//                                                    .title(place.getName() + " (Open Now)")
//                                                    .snippet("Tap marker for directions"));
//                                        }
//                                    }
//                                })
//                                .addOnFailureListener((exception) -> {
//                                    Log.e(TAG, "Error fetching place details: ", exception);
//                                });
//                    }
//
//                })
//                .addOnFailureListener((exception) -> {
//                    Log.e(TAG, "Place API call failed: ", exception);
//                    Toast.makeText(this,
//                            "Error finding places. Check API key and billing status in Google Cloud.",
//                            Toast.LENGTH_LONG).show();
//                });
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getDeviceLocationAndFindOpenPlaces();
//            } else {
//                Toast.makeText(this,
//                        "Location permission is required to find safe spots.",
//                        Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//}
package com.sankalp.womensafe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SafeSpotsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "SafeSpotsActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private static final int SEARCH_RADIUS_METERS = 1000;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String apiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_spots);

        apiKey = getString(R.string.google_maps_key);

        if (apiKey.equals("YOUR_API_KEY_HERE")) {
            Toast.makeText(this, "ERROR: Add your Google Maps API key in strings.xml", Toast.LENGTH_LONG).show();
            Log.e(TAG, "API key is not set. Please set it in strings.xml");
            finish();
            return;
        }

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocationAndFindOpenPlaces();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocationAndFindOpenPlaces() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(this, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location currentLocation = task.getResult();
                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(),
                        currentLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));

                searchNearbyPlaces(currentLatLng, "police");
                searchNearbyPlaces(currentLatLng, "hospital");
                searchNearbyPlaces(currentLatLng, "pharmacy");
                searchNearbyPlaces(currentLatLng, "restaurant");
                searchNearbyPlaces(currentLatLng, "cafe");
                searchNearbyPlaces(currentLatLng, "supermarket");
                searchNearbyPlaces(currentLatLng, "convenience_store");
            } else {
                Log.e(TAG, "Current location is null.", task.getException());
                Toast.makeText(this,
                        "Could not get current location. Make sure location is enabled.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void searchNearbyPlaces(LatLng location, String type) {
        new Thread(() -> {
            try {
                String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                        "location=" + location.latitude + "," + location.longitude +
                        "&radius=" + SEARCH_RADIUS_METERS +
                        "&type=" + type +
                        "&key=" + apiKey;

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String status = jsonResponse.getString("status");

                    if (status.equals("OK")) {
                        JSONArray results = jsonResponse.getJSONArray("results");
                        runOnUiThread(() -> processSearchResults(results, type));
                    } else if (!status.equals("ZERO_RESULTS")) {
                        Log.e(TAG, "API Error for " + type + ": " + status);
                    }
                } else {
                    Log.e(TAG, "HTTP Error: " + responseCode);
                }
                connection.disconnect();

            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error searching for " + type + ": ", e);
                runOnUiThread(() ->
                        Toast.makeText(SafeSpotsActivity.this,
                                "Error searching for " + type,
                                Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void processSearchResults(JSONArray results, String type) {
        try {
            for (int i = 0; i < results.length(); i++) {
                JSONObject place = results.getJSONObject(i);

                String name = place.optString("name", "Unknown Place");
                JSONObject location = place.getJSONObject("geometry").getJSONObject("location");
                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");

                boolean isOpen = false;
                if (place.has("opening_hours")) {
                    JSONObject openingHours = place.getJSONObject("opening_hours");
                    isOpen = openingHours.optBoolean("open_now", false);
                }

                String title = name;
                if (isOpen) {
                    title += " ✓ OPEN";
                }

                LatLng placeLatLng = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions()
                        .position(placeLatLng)
                        .title(title)
                        .snippet(formatType(type)));

                Log.d(TAG, "Added marker: " + name + " | Open: " + isOpen);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error processing results: ", e);
        }
    }

    private String formatType(String type) {
        switch (type) {
            case "police": return "Police Station";
            case "hospital": return "Hospital";
            case "pharmacy": return "Pharmacy";
            case "restaurant": return "Restaurant";
            case "cafe": return "Café";
            case "supermarket": return "Supermarket";
            case "convenience_store": return "Store";
            default: return type;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocationAndFindOpenPlaces();
            } else {
                Toast.makeText(this,
                        "Location permission is required to find safe spots.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}