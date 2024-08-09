package org.androidtracking;

import org.androidtracking.API1.BasicDeviceInfo;
import org.androidtracking.API2.AudioDeviceInfo;
import org.androidtracking.API3.SensorDeviceInfo;
import org.androidtracking.API4.WebAudioDeviceInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class Fingerprint {
    private String fingerprint;

    private JSONObject getFingerprintInfo() throws JSONException {
        DeviceInfo info1 = new BasicDeviceInfo();
        DeviceInfo info2 = new AudioDeviceInfo();
        DeviceInfo info3 = new SensorDeviceInfo();
        DeviceInfo info4 = new WebAudioDeviceInfo();

        JSONObject info4fp = new JSONObject();
        info4fp = merge(info4fp, info1.getInfo());
        info4fp = merge(info4fp, info2.getInfo());
        info4fp = merge(info4fp, info3.getInfo());
        info4fp = merge(info4fp, info4.getInfo());

        return info4fp;
    }

    private JSONObject merge(JSONObject o1, JSONObject o2) throws JSONException {
        if(o2 == null)
            return o1;
        Iterator<String> keys = o2.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            o1.put(key, o2.get(key));
        }
        return o2;
    }

    public void generateFingerprint() throws JSONException {
        JSONObject fingerprintInfo = getFingerprintInfo();
        //TODO:生成指纹。注意指纹为成员变量
    }

    public String getFingerprint(){
        return fingerprint;
    }
}
