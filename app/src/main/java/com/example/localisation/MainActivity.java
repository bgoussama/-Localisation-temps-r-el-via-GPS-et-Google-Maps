package com.example.localisation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_LOC = 100;

    private TextView tvLat;
    private TextView tvLon;

    private RequestQueue requestQueue;
    private LocationManager locationManager;

    /*
     * Pour Android Emulator :
     * 10.0.2.2 = localhost de ton PC.
     *
     * Si tu testes sur téléphone réel :
     * remplace 10.0.2.2 par l'adresse IP de ton PC.
     */
    private final String insertUrl = "http://192.168.100.168/localisation/createPosition.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLat = findViewById(R.id.tvLat);
        tvLon = findViewById(R.id.tvLon);

        Button btnMap = findViewById(R.id.btnMap);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnMap.setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));

        askLocationPermissionAndStart();
    }

    private void askLocationPermissionAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOC
            );

        } else {
            startGpsUpdates();
        }
    }

    @SuppressLint("MissingPermission")
    private void startGpsUpdates() {
        if (locationManager == null) {
            Toast.makeText(this, "LocationManager indisponible", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsEnabled && !networkEnabled) {
            buildAlertMessageNoGps();
            return;
        }

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                double alt = location.getAltitude();
                float acc = location.getAccuracy();

                tvLat.setText("Latitude: " + lat);
                tvLon.setText("Longitude: " + lon);

                String msg = String.format(
                        getResources().getString(R.string.new_location),
                        lat,
                        lon,
                        alt,
                        acc
                );

                addPosition(lat, lon);

                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                String newStatus;

                switch (status) {
                    case LocationProvider.OUT_OF_SERVICE:
                        newStatus = "OUT_OF_SERVICE";
                        break;

                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        newStatus = "TEMPORARILY_UNAVAILABLE";
                        break;

                    case LocationProvider.AVAILABLE:
                        newStatus = "AVAILABLE";
                        break;

                    default:
                        newStatus = "UNKNOWN";
                        break;
                }

                String msg = String.format(
                        getResources().getString(R.string.provider_new_status),
                        provider,
                        newStatus
                );

                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                String msg = String.format(
                        getResources().getString(R.string.provider_enabled),
                        provider
                );

                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                String msg = String.format(
                        getResources().getString(R.string.provider_disabled),
                        provider
                );

                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                buildAlertMessageNoGps();
            }
        };

        /*
         * Pour les tests, minDistance = 0.
         * Comme ça, les positions simulées dans l'émulateur remontent plus facilement.
         */
        if (gpsEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000,
                    0,
                    listener
            );
        }

        if (networkEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000,
                    0,
                    listener
            );
        }

        Toast.makeText(this, "Écoute GPS/Network démarrée", Toast.LENGTH_SHORT).show();
    }

    private void addPosition(final double lat, final double lon) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                insertUrl,
                response -> Toast.makeText(
                        getApplicationContext(),
                        "Position envoyée",
                        Toast.LENGTH_SHORT
                ).show(),

                (VolleyError error) -> Toast.makeText(
                        getApplicationContext(),
                        "Erreur réseau: " + error.toString(),
                        Toast.LENGTH_LONG
                ).show()
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();

                SimpleDateFormat sdf = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                );

                params.put("latitude", String.valueOf(lat));
                params.put("longitude", String.valueOf(lon));
                params.put("date", sdf.format(new Date()));
                params.put("imei", getDeviceIdentifier());

                return params;
            }
        };

        requestQueue.add(request);
    }

    private String getDeviceIdentifier() {
        String androidId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (androidId != null && !androidId.trim().isEmpty()) {
            return androidId;
        }

        return "UNKNOWN_DEVICE";
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_LOC) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission localisation accordée", Toast.LENGTH_SHORT).show();
                startGpsUpdates();
            } else {
                Toast.makeText(this, "Permission localisation refusée", Toast.LENGTH_LONG).show();
            }
        }
    }
}