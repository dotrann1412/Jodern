package com.example.jodern.helper;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectionsJSONParser {
    private static final String TAG = DirectionsJSONParser.class.getName();

    public List<List<HashMap<String, String>>> parsePolyline(JSONObject jObject) {
        Log.d(TAG, "parsing: " + jObject.toString());

        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;

        try {

            jRoutes = jObject.getJSONArray("routes");
            Log.d(TAG, "parse: JSON route " + jRoutes);

            /* Traversing all routes */
//            for (int i = 0; i < jRoutes.length(); i++) {
            int i = 0;
            jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
            Log.d(TAG, "parse: JSON route " + i + ": " + jLegs);
            List<HashMap<String, String>> path = new ArrayList<>();

            /* Traversing all legs */
            for (int j = 0; j < jLegs.length(); j++) {
                Log.d(TAG, "parse: JSON route traversing leg " + j);
                jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                Log.d(TAG, "parse: traversed step " + jSteps);

                /* Traversing all steps */
                for (int k = 0; k < jSteps.length(); k++) {
                    try {
                        JSONObject maneuver = (JSONObject) ((JSONObject) jSteps.get(k)).get("maneuver");
                        Log.i(TAG, "parse: maneuver: " + maneuver);
                        JSONArray locations = (JSONArray) maneuver.get("location");
                        Log.i(TAG, "parse: location: " + locations);
                        HashMap<String, String> hm = new HashMap<>();
                        hm.put("lat", Double.toString((Double) locations.get(1)));
                        hm.put("lng", Double.toString((Double) locations.get(0)));
                        path.add(hm);
                    } catch (JSONException e) {
                        Log.e(TAG, "parse: maneuver is not existed");
                    }

                    JSONArray intersections = (JSONArray) ((JSONObject) jSteps.get(k)).get("intersections");
                    Log.i(TAG, "parse: intersection: " + intersections);
                    for (int l = 0; l < intersections.length(); ++l) {
                        JSONArray locations = (JSONArray) ((JSONObject)intersections.get(l)).get("location");
                        Log.i(TAG, "parse: location: " + locations);
                        HashMap<String, String> hm = new HashMap<>();
                        hm.put("lat", Double.toString((Double) locations.get(1)));
                        hm.put("lng", Double.toString((Double) locations.get(0)));
                        path.add(hm);
                    }
                }
                routes.add(path);
            }

        } catch (JSONException e) {
            Log.e(TAG, "parse: JSONException: failed with error: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "parse: Exception: failed with error: " + e.getMessage());
        }

        return routes;
    }

    public List<List<HashMap<String, String>>> parseGeoJSON(JSONObject jObject) {
        Log.d(TAG, "parsing: " + jObject.toString());

        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONArray jRoutes;
        JSONArray jCoords;

        try {

            jRoutes = jObject.getJSONArray("routes");
            Log.d(TAG, "parse: JSON route " + jRoutes);

            int i = 0;
            jCoords = ((JSONObject) ((JSONObject) jRoutes.get(i)).get("geometry")).getJSONArray("coordinates");
            Log.d(TAG, "parse: JSON route coordinates " + jCoords);
            List<HashMap<String, String>> path = new ArrayList<>();

            /* Traversing all legs */
            for (int j = 0; j < jCoords.length(); j++) {
                JSONArray coord = (JSONArray) jCoords.get(j);
                HashMap<String, String> hm = new HashMap<>();
                hm.put("lat", Double.toString((Double) coord.get(1)));
                hm.put("lng", Double.toString((Double) coord.get(0)));
                path.add(hm);
                routes.add(path);
            }

        } catch (JSONException e) {
            Log.e(TAG, "parse: JSONException: failed with error: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "parse: Exception: failed with error: " + e.getMessage());
        }

        return routes;
    }
}
