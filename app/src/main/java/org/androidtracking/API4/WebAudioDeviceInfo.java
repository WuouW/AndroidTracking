package org.androidtracking.API4;

import android.util.Log;

import org.androidtracking.DeviceInfo;
import org.json.JSONException;
import org.json.JSONObject;


public class WebAudioDeviceInfo implements DeviceInfo {
    public static final String TAG = "WebAudioDeviceInfo";

    @Override
    public JSONObject getInfo() {

        JSONObject basicInfo = new JSONObject();
        AudioFingerprint.generateFingerprint(fingerprint -> {
            Log.d(TAG, "Fingerprint: " + fingerprint);
            try {
                basicInfo.put("WebAudioDeviceInfo", fingerprint);
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
        });

        Log.d(TAG, "basicInfo: " + basicInfo);
        return basicInfo;
    }


}
