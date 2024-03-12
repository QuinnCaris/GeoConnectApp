package com.example.geoconnectapp.logic;

import android.util.Log;

import com.google.firebase.firestore.GeoPoint;

import java.util.Map;

public class Tracking {

    private Map<String, Object> geocacheDoc;

    public Tracking(Map<String, Object> geocacheDoc) {
        this.geocacheDoc = geocacheDoc;
    }

    public double calculateAngleDiff(double userLat, double userLong) {
        double angleUser = SensorHandler.updateOrientationAngles();
        double cacheLat = ((GeoPoint) geocacheDoc.get("location")).getLatitude();
        double cacheLong = ((GeoPoint) geocacheDoc.get("location")).getLongitude();
        double angleCache = Math.toDegrees(Math.atan2(cacheLat - userLat, cacheLong - userLong));
        Log.d("Tracking", "tan is returning " + angleCache);
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
}
