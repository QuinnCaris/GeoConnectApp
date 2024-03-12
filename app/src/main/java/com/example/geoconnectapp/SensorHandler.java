package com.example.geoconnectapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorHandler implements SensorEventListener {
    private static final float[] accelerometerReading = new float[3];
    private static final float[] magnetometerReading = new float[3];
    private static final float[] rotationMatrix = new float[9];
    private static final float[] orientationAngles = new float[3];

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }
    }

    public static double updateOrientationAngles() {
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        double actualAngle = (orientationAngles[0] / Math.PI) * 180;
        // You can handle or broadcast the orientation angle as needed.
        return actualAngle;
    }
}

