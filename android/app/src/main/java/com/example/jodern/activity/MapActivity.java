package com.example.jodern.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.example.jodern.BuildConfig;
import com.example.jodern.R;
import com.example.jodern.adapter.MapMarkerInfoAdapter;
import com.example.jodern.customwidget.MySnackbar;

import com.example.jodern.helper.FetchURL;
import com.example.jodern.helper.TaskLoadedCallback;
import com.example.jodern.model.BranchLocation;
import com.example.jodern.provider.Provider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Objects;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {
    private static final String TAG = "Map";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int DEFAULT_ZOOM = 13;

    private Boolean locationPermissionGranted = false;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private BranchLocation nearestBranch;
    private BranchLocation currentLocation;

    private LinearLayout mapParentView;
    private LinearLayout loadingWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: creating map activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        nearestBranch = null;

        mapParentView = findViewById(R.id.mapParentView);
        loadingWrapper = findViewById(R.id.mapLoadingWrapper);
        loadingWrapper.setVisibility(View.VISIBLE);


        getLocationPermission();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Google Map is now ready");
        this.googleMap = googleMap;
        this.googleMap.setInfoWindowAdapter(new MapMarkerInfoAdapter(MapActivity.this));

        if (locationPermissionGranted) {
            getDeviceLocation();
            googleMap.setMyLocationEnabled(true);

            if (nearestBranch != null) {
                moveCamera(new LatLng(nearestBranch.getLatitude(), nearestBranch.getLongitude()));
            }
        }
    }

    private void retrieveBranchLocation() {
        Log.d(TAG, "retrieveBranchLocation: retrieving the branch locations data");
        String url = BuildConfig.SERVER_URL + "stores-location";
        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseLocationJSON(response);
                        Log.d(TAG, "onResponse: successful");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        MySnackbar.inforSnackar(MapActivity.this, mapParentView, getString(R.string.error_message)).show();
                        Log.d(TAG, "onErrorResponse: VolleyError: " + error);
                        loadingWrapper.setVisibility(View.GONE);
                    }
                }
        );
        Provider.with(MapActivity.this).addToRequestQueue(stringRequest);
    }

    private void parseLocationJSON(JSONObject response) {
        try {
            JSONArray locations = response.getJSONArray("location");
            for (int i = 0; i < locations.length(); ++i) {
                JSONArray latLng = (JSONArray) locations.get(i);
                BranchLocation location = new BranchLocation((double) latLng.get(0), (double) latLng.get(1));

                setBranchMarker(location);

                if (i == 0) {
                    Log.d(TAG, "parseLocationJSON: initialize nearest branch (" + location.getLatitude() + ", " + location.getLongitude() + ")");
                    nearestBranch = location;
                } else {
                    if (calculationByDistance(BranchLocation.toLatLng(nearestBranch), BranchLocation.toLatLng(currentLocation))
                            > calculationByDistance(BranchLocation.toLatLng(location), BranchLocation.toLatLng(currentLocation))) {
                        Log.d(TAG, "parseLocationJSON: modify nearest branch (" + location.getLatitude() + ", " + location.getLongitude() + ")");
                        nearestBranch = location;
                    }
                }
            }
            moveCamera(BranchLocation.toLatLng(nearestBranch));
        } catch (Exception e) {
            Log.d(TAG, "parseLocationJSON: " + e.getMessage());
        } finally {
            loadingWrapper.setVisibility(View.GONE);
        }
    }

    private double calculationByDistance(@NonNull LatLng StartP, @NonNull LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.parseInt(newFormat.format(valueResult));
        double meter = valueResult % 1000;
        int meterInDec = Integer.parseInt(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    private void setBranchMarker(@NonNull BranchLocation branchLocation) {
        Log.d(TAG, "setBranchMarker: setting markers for (" + branchLocation.getLatitude() + ", " + branchLocation.getLongitude() + ")");
        MarkerOptions options = new MarkerOptions()
                .position(BranchLocation.toLatLng(branchLocation))
                .title("Jodern")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_logo_icon));

        Objects.requireNonNull(googleMap.addMarker(options)).setTag(branchLocation);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), permissions[1]) == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                initMap();

            }
            else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void initMap() {
        try {
            Log.d(TAG, "initMap: initializing map");
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void moveCamera(@NonNull LatLng latLng) {
        Log.d(TAG, "moveCamera: move the camera to: (lat=" + latLng.latitude + ",lng=" + latLng.longitude + ")");
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }

    private String getDirectionUrl(LatLng origin, LatLng dest, String directionMode) {
        Log.d(TAG, "getDirectionUrl: origin = " + origin + ", destination = " + dest);
        String originStr = "origin=" + origin.latitude + "," + origin.longitude;
        String destStr = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=" + directionMode;
        String parameters = originStr + "&" + destStr + "&" + mode;
        String output = "json";
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key" + BuildConfig.MAPS_API_KEY;
    }

    private void getPathToLocation(LatLng destination, String directionMode) {
        Log.d(TAG, "getPathToLocation: getting path to location " + destination);
        String url = getDirectionUrl(BranchLocation.toLatLng(currentLocation), destination, directionMode);
        new FetchURL(MapActivity.this).execute(url, directionMode);
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting device location");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (locationPermissionGranted) {
                @SuppressLint("MissingPermission") Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: found location!");
                        currentLocation = BranchLocation.fromLocation((Location) task.getResult());
                        moveCamera(BranchLocation.toLatLng(currentLocation));
                        retrieveBranchLocation();
                        getPathToLocation(new LatLng(10.7314940, 106.6966400), "driving");
                    } else {
                        Log.d(TAG, "onComplete: current location is null");
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: Security Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "getDeviceLocation: Exception");
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; ++i) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            locationPermissionGranted = false;
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    locationPermissionGranted = true;
                    initMap();
                }
            }
        }
    }

    @Override
    public void onTaskDone(Object... values) {
        Log.d(TAG, "onTaskDone: task is done, drawing a polyline");
        if (currentPolyline != null) {
            currentPolyline.remove();
        }
        currentPolyline = googleMap.addPolyline( (PolylineOptions) values[0]);
    }

    private Polyline currentPolyline;
}