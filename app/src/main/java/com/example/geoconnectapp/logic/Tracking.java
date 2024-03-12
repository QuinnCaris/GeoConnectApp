package com.example.geoconnectapp.logic;

import android.util.Log;

import com.google.firebase.firestore.GeoPoint;

import java.util.Map;

public class Tracking {

    final double RADIUS = 6371000;
    private Map<String, Object> geocacheDoc;
    private double cacheLat;
    private double cacheLong;

    public Tracking(Map<String, Object> geocacheDoc) {
        this.geocacheDoc = geocacheDoc;
        this.cacheLat = ((GeoPoint) geocacheDoc.get("location")).getLatitude();
        cacheLong = ((GeoPoint) geocacheDoc.get("location")).getLongitude();
    }

    public double calculateAngleDiff(double userLat, double userLong) {
        double angleUser = SensorHandler.updateOrientationAngles();
        double angleCache = Math.toDegrees(Math.atan2(cacheLat - userLat, cacheLong - userLong));
        Log.d("Tracking", "tan is returning " + angleCache);

        // DONT TOUCH THESE TRANSFORMATIONS.
        // I've debugged these for an hour and this works so don't touch it unless you're ready to face the consequences.
        angleCache = angleCache + 270;
        angleCache = angleCache % 360;
        angleCache = 360 - angleCache;
        Log.d("Tracking", "Angle of cache: " + angleCache);
        Log.d("Tracking", "Angle of user: " + angleUser);
        // Calculate angle difference
        double angleDiff = angleCache - angleUser;
        if (angleDiff < 0) {
            angleDiff = 360 + angleDiff;
        }
        return angleDiff % 360;
    }

    // Calculates distance between two points with haversine formula
    public double calculateDistance(double userLat, double userLong) {
        // distance between latitudes and longitudes
        double dLat = Math.toRadians(cacheLat - userLat);
        double dLon = Math.toRadians(cacheLong - userLong);

        // convert to radians
        double userLat2 = Math.toRadians(userLat);
        double cacheLat2 = Math.toRadians(cacheLat);

        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(userLat2) *
                        Math.cos(cacheLat2);
        double rad = 6371000;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
    }
}
