package org.androidtracking.API3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;

public class Gyroscope {
    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private SensorEventListener gyroscopeListener;
    private float[] gyroscopeData = new float[3];
    private boolean isCollecting = false;

    public Gyroscope(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    public void startCollecting(GyroscopeDataCallback callback) {
        if (isCollecting){
            return;
        }

        gyroscopeListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    gyroscopeData[0] = event.values[0];
                    gyroscopeData[1] = event.values[1];
                    gyroscopeData[2] = event.values[2];

                    callback.onGyroscopeDataChanged(gyroscopeData[0], gyroscopeData[1], gyroscopeData[2]);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                return;
            }
        };

        sensorManager.registerListener(gyroscopeListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
        isCollecting = true;

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable task = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 5000);
                stopCollecting();
                callback.onDataCollectionFinished();
            }
        };
        task.run();
        /*handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopCollecting();
                callback.onDataCollectionFinished();
            }
        }, 5000);*/
    }

    public void stopCollecting() {
        if (!isCollecting){
            return;
        }
        sensorManager.unregisterListener(gyroscopeListener);
        isCollecting = false;
    }

    public interface GyroscopeDataCallback {
        void onGyroscopeDataChanged(float x, float y, float z);
        void onDataCollectionFinished();
    }
}
