package org.androidtracking;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;
import android.view.View;

import org.androidtracking.API1.BasicDeviceInfo;
import org.androidtracking.API2.AudioDeviceInfo;
import org.androidtracking.API3.SensorDeviceInfo;
import org.androidtracking.API4.WebAudioDeviceInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class Fingerprint {
    private String fingerprint;

    private static final String TAG = "Fingerprint";
    private final Context context;
    private final Activity activity;

    private JSONObject fingerprintInfo;

    public Fingerprint(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public interface GenerateFingerprintInfoCallback {
        void onFinished(JSONObject jsonObject);

    }

    public void generateFingerprintInfo(GenerateFingerprintInfoCallback callbackFunc) {
        new Thread(() -> {
            DeviceInfo info1 = new BasicDeviceInfo();
            DeviceInfo info2 = new AudioDeviceInfo();

            CountDownLatch latch = new CountDownLatch(2);
            DeviceInfo info3 = new SensorDeviceInfo();
            DeviceInfo info4 = new WebAudioDeviceInfo();

            ((BasicDeviceInfo) info1).setContextActivity(context, activity);
            ((SensorDeviceInfo) info3).setContext(context);
            ((SensorDeviceInfo) info3).setLatch(latch);

            JSONObject info4fp = new JSONObject();
            JSONObject info3Res = info3.getInfo();
            CompletableFuture<Void> future = CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e);
                    }
                    System.out.println("finish all sensors");
                }
            });
            try {
                info4fp = merge(info4fp, info1.getInfo());
                info4fp = merge(info4fp, info2.getInfo());
                future.get();
                Log.d("result", "info3Res: " + info3Res.toString());
                info4fp = merge(info4fp, info3Res);
                JSONObject info4Res = info4.getInfo();
                Log.d("result", "info4Res: " + info4Res.toString());
                info4fp = merge(info4fp, info4Res);
                fingerprintInfo = info4fp;
                callbackFunc.onFinished(info4fp);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

        }).start();

    }

    private JSONObject merge(JSONObject o1, JSONObject o2) throws JSONException {
        if (o2 == null)
            return o1;
        Iterator<String> keys = o2.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            o1.put(key, o2.get(key));
        }
        return o1;
    }


    public JSONObject getFingerprintInfo() {
        return fingerprintInfo;
    }

    public String getFingerprint() {
        return fingerprint;
    }
}
