package com.example.jodernstore.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import com.example.jodernstore.BuildConfig;
import com.example.jodernstore.R;
import com.example.jodernstore.adapter.MapMarkerInfoAdapter;
import com.example.jodernstore.customwidget.MySnackbar;

import com.example.jodernstore.helper.DirectionsJSONParser;
import com.example.jodernstore.model.BranchLocation;
import com.example.jodernstore.provider.Provider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = MapActivity.class.getName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int DEFAULT_ZOOM = 13;

    private Boolean locationPermissionGranted = false;
    private GoogleMap googleMap;

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
                response -> {
                    parseLocationJSON(response);
                    Log.d(TAG, "onResponse: successful");
                },
                error -> {
                    MySnackbar.inforSnackar(MapActivity.this, mapParentView, getString(R.string.error_message)).show();
                    Log.d(TAG, "onErrorResponse: VolleyError: " + error);
                    loadingWrapper.setVisibility(View.GONE);
                }
        );
        Provider.with(MapActivity.this).addToRequestQueue(stringRequest);
    }

    private void parseLocationJSON(JSONObject response) {
        try {
            JSONArray branches = response.getJSONArray("branchs");
            for (int i = 0; i < branches.length(); ++i) {
                JSONObject branch = (JSONObject) branches.get(i);
                JSONArray latLng = (JSONArray) branch.get("coordinate");
                BranchLocation location = new BranchLocation((double) latLng.get(0), (double) latLng.get(1));
                setBranchMarker(location);
                if (i == 0) {
                    Log.d(TAG, "parseLocationJSON: initialize nearest branch " + location);
                    nearestBranch = location;
                } else {
                    if (calculationByDistance(BranchLocation.toLatLng(nearestBranch), BranchLocation.toLatLng(currentLocation))
                            > calculationByDistance(BranchLocation.toLatLng(location), BranchLocation.toLatLng(currentLocation))) {
                        Log.d(TAG, "parseLocationJSON: modify nearest branch " + location);
                        nearestBranch = location;
                    }
                }
            }
            moveCamera(BranchLocation.toLatLng(nearestBranch));
            Log.d(TAG, "parseLocationJSON: done with move camera to nearest branch, trying to draw the path");
//            getPathToLocation(new LatLng(10.7314940, 106.6966400));
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

            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
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
        Log.d(TAG, "moveCamera: move the camera to: " + latLng);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        Log.d(TAG, "getDirectionUrl: origin = " + origin + ", destination = " + dest);
        String originStr = origin.longitude + "," + origin.latitude;
        String destStr = dest.longitude + "," + dest.latitude;
        return "https://api.mapbox.com/directions/v5/mapbox/driving/" + originStr + ";" + destStr + "?alternatives=true&geometries=geojson&language=en&overview=simplified&steps=true&access_token=" + getString(R.string.mapbox_access_token);
    }

    private void getPathToLocation(LatLng destination) {
        Log.d(TAG, "getPathToLocation: getting path to location " + destination);
        String url = getDirectionsUrl(BranchLocation.toLatLng(currentLocation), destination);
        Log.d(TAG, "getPathToLocation: direction URL is " + url);
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting device location");
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (locationPermissionGranted) {
                @SuppressLint("MissingPermission") Task<Location> location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentLocation = BranchLocation.fromLocation((Location) task.getResult());
                        Log.d(TAG, "onComplete: current location " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                        moveCamera(BranchLocation.toLatLng(currentLocation));
                        retrieveBranchLocation();
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
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
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

    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            Log.d(TAG, "Background Task: doing with " + url[0]);
            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            Log.d(TAG, "Background Task: done and return data = " + data);
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute: " + result);
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }


    /**
     * A class to parse the Mapbox Direction API result in JSON format
     */
    @SuppressLint("StaticFieldLeak")
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            Log.d(TAG, "ParserTask background task: " + jsonData[0]);
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parseGeoJSON(jObject);
            } catch (Exception e) {
                Log.e(TAG, "ParserTask background task: " + e.getMessage());
                return null;
            }
            Log.d(TAG, "ParserTask post background task: " + routes.toString());
            return routes;
        }

        @Override
        protected void onPostExecute(@NonNull List<List<HashMap<String, String>>> result) {
            Log.d(TAG, "onPostExecute: result is not null: " + result);
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                    double lng = Double.parseDouble(Objects.requireNonNull(point.get("lng")));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }

            // Drawing polyline in the Google Map for the i-th route
            assert lineOptions != null;
            googleMap.addPolyline(lineOptions);
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}