package org.androidtracking;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.androidtracking.API1.BasicDeviceInfo;
import org.androidtracking.API2.AudioDeviceInfo;
import org.androidtracking.API3.SensorDeviceInfo;
import org.androidtracking.API4.WebAudioDeviceInfo;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fingerprint fp = new Fingerprint();
        String fingerprint = fp.getFingerprint();

        //TODO:可选：指纹的展示
    }
}