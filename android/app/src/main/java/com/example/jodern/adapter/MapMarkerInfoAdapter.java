package com.example.jodern.adapter;

import android.content.Context;

import com.example.jodern.R;
import com.example.jodern.model.BranchLocation;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class MapMarkerInfoAdapter implements GoogleMap.InfoWindowAdapter {
    private final Context context;

    public MapMarkerInfoAdapter(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        BranchLocation place = (BranchLocation) marker.getTag();
        View view = LayoutInflater.from(context).inflate(R.layout.map_marker_info_content, null);
        // set something
        return view;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        return null;
    }
}
