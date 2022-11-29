package android.jodern.app.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class BranchLocation {
    private double latitude;
    private double longitude;

    public BranchLocation() {

    }
    public BranchLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
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

    public static BranchLocation getCenter(List<BranchLocation> branchLocations) {
        if (branchLocations == null || branchLocations.isEmpty()) {
            return null;
        }
        BranchLocation centerLocation = new BranchLocation();
        for (BranchLocation location : branchLocations) {
            centerLocation.setLatitude(centerLocation.getLatitude() + location.getLatitude());
            centerLocation.setLongitude(centerLocation.getLongitude() + location.getLongitude());
        }
        centerLocation.setLatitude(centerLocation.getLatitude() / branchLocations.size());
        centerLocation.setLongitude(centerLocation.getLongitude() / branchLocations.size());
        return centerLocation;
    }

    public static LatLng toLatLng(BranchLocation branchLocation) {
        return new LatLng(branchLocation.getLatitude(), branchLocation.getLongitude());
    }

    public static BranchLocation fromLocation(Location location) {
        return new BranchLocation((double) location.getLatitude(), (double) location.getLongitude());
    }
}
