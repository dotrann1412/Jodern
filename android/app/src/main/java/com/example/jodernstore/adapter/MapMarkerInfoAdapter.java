package com.example.jodernstore.adapter;

import android.content.Context;

import com.example.jodernstore.R;
import com.example.jodernstore.model.BranchInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

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
        BranchInfo place = (BranchInfo) marker.getTag();
        View view = LayoutInflater.from(context).inflate(R.layout.map_marker_info_content, null);
        assert place != null;
        ((TextView)view.findViewById(R.id.markerBranchName)).setText(place.getBranchName());
        return view;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        return null;
    }
}
