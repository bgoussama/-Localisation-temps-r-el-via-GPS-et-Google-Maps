package com.example.localisation;

import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private RequestQueue requestQueue;

    /*
     * Pour Android Emulator :
     * 10.0.2.2 = localhost du PC vu depuis l'émulateur.
     *
     * Si tu utilises un vrai téléphone :
     * remplace 10.0.2.2 par l'adresse IP de ton PC.
     */
    private final String showUrl = "http://192.168.100.168/localisation/showPositions.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "MapFragment introuvable", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Toast.makeText(this, "Map Ready", Toast.LENGTH_SHORT).show();

        setUpMap();
    }

    private void setUpMap() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                showUrl,
                null,
                response -> {
                    try {
                        JSONArray positions = response.getJSONArray("positions");

                        if (positions.length() == 0) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Aucune position en base",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        LatLng lastPosition = null;

                        for (int i = 0; i < positions.length(); i++) {
                            JSONObject position = positions.getJSONObject(i);

                            double lat = position.getDouble("latitude");
                            double lon = position.getDouble("longitude");
                            String date = position.optString("date", "");

                            LatLng pos = new LatLng(lat, lon);

                            mMap.addMarker(
                                    new MarkerOptions()
                                            .position(pos)
                                            .title("Position")
                                            .snippet(date)
                            );

                            lastPosition = pos;
                        }

                        if (lastPosition != null) {
                            mMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(lastPosition, 15f)
                            );
                        }

                        Toast.makeText(
                                getApplicationContext(),
                                "Markers chargés : " + positions.length(),
                                Toast.LENGTH_SHORT
                        ).show();

                    } catch (JSONException e) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Erreur JSON : " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                },
                error -> Toast.makeText(
                        getApplicationContext(),
                        "Erreur réseau Map : " + error.toString(),
                        Toast.LENGTH_LONG
                ).show()
        );

        requestQueue.add(jsonObjectRequest);
    }
}