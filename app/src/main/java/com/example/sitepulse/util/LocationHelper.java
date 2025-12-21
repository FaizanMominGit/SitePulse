package com.example.sitepulse.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationHelper {

    private FusedLocationProviderClient fusedLocationClient;

    public LocationHelper(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation(OnSuccessListener<Location> onSuccess, OnFailureListener onFailure) {
        // Retrieve the last known location, or request a new one if stale
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public static float getDistanceInMeters(double startLat, double startLng, double endLat, double endLng) {
        float[] results = new float[1];
        Location.distanceBetween(startLat, startLng, endLat, endLng, results);
        return results[0];
    }
    
    public static boolean isUserOnSite(double userLat, double userLng, double siteLat, double siteLng, double radiusMeters) {
        float distance = getDistanceInMeters(userLat, userLng, siteLat, siteLng);
        return distance <= radiusMeters;
    }
}