package com.example.jodernstore.model;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class BranchInfo {
    private double latitude;
    private double longitude;
    private String branchName;
    private int branchId;

    public BranchInfo() {

    }

    public BranchInfo(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public BranchInfo(double latitude, double longitude, String branchName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.branchName = branchName;
    }

    public BranchInfo(int branchId, String branchName) {
        this.branchName = branchName;
        this.branchId = branchId;
    }

    public BranchInfo(double latitude, double longitude, String branchName, int branchId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.branchName = branchName;
        this.branchId = branchId;
    }

    public int getBranchId() {
        return branchId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getBranchName() {
        return branchName;
    }

    public static BranchInfo parseJSON(JSONObject response) {
        try {
            int id = response.getInt("branch_id");
            String name = response.getString("branch_name");
            JSONArray coordinate = (JSONArray) response.get("coordinate");
            Double latitude = coordinate.getDouble(0);
            Double longitude = coordinate.getDouble(1);
            return new BranchInfo(latitude, longitude, name, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BranchInfo getCenter(List<BranchInfo> branchInfoList) {
        if (branchInfoList == null || branchInfoList.isEmpty()) {
            return null;
        }
        BranchInfo centerLocation = new BranchInfo();
        for (BranchInfo location : branchInfoList) {
            centerLocation.setLatitude(centerLocation.getLatitude() + location.getLatitude());
            centerLocation.setLongitude(centerLocation.getLongitude() + location.getLongitude());
        }
        centerLocation.setLatitude(centerLocation.getLatitude() / branchInfoList.size());
        centerLocation.setLongitude(centerLocation.getLongitude() / branchInfoList.size());
        return centerLocation;
    }

    public static LatLng toLatLng(BranchInfo branchInfo) {
        return new LatLng(branchInfo.getLatitude(), branchInfo.getLongitude());
    }

    public static BranchInfo fromLocation(@NonNull Location location) {
        return new BranchInfo(location.getLatitude(), location.getLongitude());
    }
}